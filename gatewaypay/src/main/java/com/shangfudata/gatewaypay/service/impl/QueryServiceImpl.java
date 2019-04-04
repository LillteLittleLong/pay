package com.shangfudata.gatewaypay.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;

import com.shangfudata.gatewaypay.dao.DistributionInfoRespository;
import com.shangfudata.gatewaypay.dao.DownMchBusiInfoRepository;
import com.shangfudata.gatewaypay.dao.GatewaypayInfoRepository;
import com.shangfudata.gatewaypay.dao.UpMchBusiInfoRepository;
import com.shangfudata.gatewaypay.entity.*;
import com.shangfudata.gatewaypay.service.NoticeService;
import com.shangfudata.gatewaypay.service.QueryService;
import com.shangfudata.gatewaypay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {

    String queryUrl = "http://192.168.88.65:8888/gate/spsvr/order/qry";
    String signKey = "00000000000000000000000000000000";

    @Autowired
    GatewaypayInfoRepository gatewaypayInfoRepository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;
    @Autowired
    DistributionInfoRespository distributionInfoRespository;
    @Autowired
    NoticeService noticeService;

    /**
     * 向上查询（轮询方法）
     */
    @Scheduled(cron = "*/5 * * * * ?")
    public void queryToUp() {

        Gson gson = new Gson();

        //查询所有交易状态为NOTPAY的订单信息
        List<GatewaypayInfo> distillpayInfoList = gatewaypayInfoRepository.findByTradeState("NOTPAY");

        //遍历
        for (GatewaypayInfo gatewaypayInfo : distillpayInfoList) {
            //判断处理状态为SUCCESS的才进行下一步操作
            if ("SUCCESS".equals(gatewaypayInfo.getStatus())) {
                //查询参数对象
                QueryInfo queryInfo = new QueryInfo();
                queryInfo.setMch_id(gatewaypayInfo.getMch_id());
                queryInfo.setNonce_str(gatewaypayInfo.getNonce_str());
                queryInfo.setOut_trade_no(gatewaypayInfo.getOut_trade_no());

                //将queryInfo转为json，再转map
                String query = gson.toJson(queryInfo);
                Map queryMap = gson.fromJson(query, Map.class);
                //签名
                queryMap.put("sign", SignUtils.sign(queryMap, signKey));

                //发送查询请求，得到响应信息
                String queryResponse = HttpUtil.post(queryUrl, queryMap, 6000);
                System.out.println("查询响应信息：：" + queryResponse);
                //使用一个新的UpdistillpayInfo对象，接收响应参数
                GatewaypayInfo responseInfo = gson.fromJson(queryResponse, GatewaypayInfo.class);
                //如果交易状态发生改变，那就更新。
                if (!(responseInfo.getTrade_state().equals(gatewaypayInfo.getTrade_state()))) {

                    //得到订单号
                    String out_trade_no = gatewaypayInfo.getOut_trade_no();
                    String status = responseInfo.getStatus();

                    //得到正确交易状态信息
                    String trade_state = responseInfo.getTrade_state();
                    String err_code = responseInfo.getErr_code();
                    String err_msg = responseInfo.getErr_msg();
                    String settle_state = responseInfo.getSettle_state();
                    String settle_state_desc = responseInfo.getSettle_state_desc();
                    String ch_trade_no = responseInfo.getCh_trade_no();

                    //失败信息
                    String code = responseInfo.getCode();
                    String message = responseInfo.getMessage();


                    //根据订单号，更新数据库交易信息表
                    if ("SUCCESS".equals(status)) {
                        //将订单信息表存储数据库
                        gatewaypayInfoRepository.updateSuccessTradeState(trade_state, err_code, err_msg, out_trade_no);
                        System.out.println("开始清分");
                        //如果交易状态为成功，做清分
                        if ("SUCCESS".equals(trade_state)) {
                            // 网关清分
                            GatewaypayInfo byOutTradeNo = gatewaypayInfoRepository.findByOutTradeNo(out_trade_no);
                            distribution(byOutTradeNo);
                        }
                    } else if ("FAIL".equals(status)) {
                        gatewaypayInfoRepository.updateFailTradeState(status, code, message, out_trade_no);
                    }
                    GatewaypayInfo byOutTradeNo = gatewaypayInfoRepository.findByOutTradeNo(out_trade_no);
                    noticeService.ToDown(byOutTradeNo);
                }
            }
        }
    }


    /**
     * 下游查询方法
     *
     * @param gatewaypayInfoToJson
     */
    //@Cacheable(value = "collpay", key = "#order.outTradeNo", unless = "#result.tradeState eq 'PROCESSING'")
    public String downQuery(String gatewaypayInfoToJson) {
        Gson gson = new Gson();
        GatewaypayInfo gatewaypayInfo = gson.fromJson(gatewaypayInfoToJson, GatewaypayInfo.class);
        String out_trade_no = gatewaypayInfo.getOut_trade_no();

        GatewaypayInfo finalGatewaypayInfo = gatewaypayInfoRepository.findByOutTradeNo(out_trade_no);

        return gson.toJson(finalGatewaypayInfo);
    }

    /**
     * 清分方法
     *
     * @param gatewaypayInfo
     */
    public void distribution(GatewaypayInfo gatewaypayInfo) {
        //计算清分,需要拿到{上游商户的手续费率，下游商户的手续费率}
        DownMchBusiInfo downMchBusiInfo = downMchBusiInfoRepository.getOne(gatewaypayInfo.getDown_busi_id());
        UpMchBusiInfo upMchBusiInfo = upMchBusiInfoRepository.getOne(gatewaypayInfo.getUp_busi_id());

        //交易金额
        BigDecimal Total_fee = Convert.toBigDecimal(gatewaypayInfo.getTotal_fee());
        //获取上下游商户最低手续费及相应手续费率
        BigDecimal down_commis_charge = Convert.toBigDecimal(downMchBusiInfo.getCommis_charge());
        BigDecimal down_min_charge = Convert.toBigDecimal(downMchBusiInfo.getMin_charge());
        BigDecimal up_commis_charge = Convert.toBigDecimal(upMchBusiInfo.getCommis_charge());
        BigDecimal up_min_charge = Convert.toBigDecimal(upMchBusiInfo.getMin_charge());

        //定义上下游最终手续费
        int i = down_commis_charge.multiply(Total_fee).compareTo(down_min_charge);
        int j = up_commis_charge.multiply(Total_fee).compareTo(up_min_charge);

        BigDecimal final_down_charge;
        if ((-1) == i) {
            final_down_charge = down_min_charge;
        } else if ((1) == i) {
            final_down_charge = down_commis_charge.multiply(Total_fee);
        } else {
            final_down_charge = down_min_charge;
        }
        BigDecimal final_up_charge;
        if ((-1) == j) {
            final_up_charge = up_min_charge;
        } else if ((1) == j) {
            final_up_charge = up_commis_charge.multiply(Total_fee);
        } else {
            final_up_charge = up_min_charge;
        }

        //计算利润,先四舍五入小数位
        final_down_charge = final_down_charge.setScale(0, BigDecimal.ROUND_HALF_UP);
        final_up_charge = final_up_charge.setScale(0, BigDecimal.ROUND_HALF_UP);
        BigDecimal profit = final_down_charge.subtract(final_up_charge);

        DistributionInfo distributionInfo = new DistributionInfo();
        distributionInfo.setOut_trade_no(gatewaypayInfo.getOut_trade_no());
        distributionInfo.setBusi_type("gatewaypay");
        distributionInfo.setDown_mch_id(gatewaypayInfo.getDown_mch_id());
        distributionInfo.setDown_charge(final_down_charge.toString());
        distributionInfo.setUp_mch_id(gatewaypayInfo.getMch_id());
        distributionInfo.setUp_charge(final_up_charge.toString());
        distributionInfo.setProfit(profit.toString());
        distributionInfo.setTrad_amount(Total_fee.toString());

        //存数据库
        distributionInfoRespository.save(distributionInfo);
    }

}