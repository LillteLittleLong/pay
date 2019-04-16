package com.shangfudata.gatewaypay.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.gatewaypay.dao.*;
import com.shangfudata.gatewaypay.entity.*;
import com.shangfudata.gatewaypay.service.NoticeService;
import com.shangfudata.gatewaypay.service.QueryService;
import com.shangfudata.gatewaypay.util.SignUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {

    String queryUrl = "http://192.168.88.65:8888/gate/spsvr/order/qry";

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
    @Autowired
    UpMchInfoRepository upMchInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 向上查询（轮询方法）
     */
    @Scheduled(cron = "0 */5 * * * ?")
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

                // 获取上游商户信息
                UpMchInfo upMchInfo = upMchInfoRepository.queryByMchId(gatewaypayInfo.getMch_id());
                //签名
                queryMap.put("sign", SignUtils.sign(queryMap, upMchInfo.getSign_key()));
                logger.info("向上查询请求信息："+queryMap);

                //发送查询请求，得到响应信息
                String queryResponse = HttpUtil.post(queryUrl, queryMap, 6000);
                if(null == queryResponse){
                    logger.error("向上查询请求失败：");
                }else{
                    logger.info("向上查询请求响应信息："+queryResponse);
                }

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
                    //String settle_state = responseInfo.getSettle_state();
                    //String settle_state_desc = responseInfo.getSettle_state_desc();
                    //String ch_trade_no = responseInfo.getCh_trade_no();

                    //失败信息
                    String code = responseInfo.getCode();
                    String message = responseInfo.getMessage();

                    //根据订单号，更新数据库交易信息表
                    if ("SUCCESS".equals(status)) {
                        //将订单信息表存储数据库
                        gatewaypayInfoRepository.updateSuccessTradeState(trade_state, err_code, err_msg, out_trade_no);

                        //如果交易状态为成功，做清分
                        if ("SUCCESS".equals(trade_state)) {
                            // 网关清分
                            GatewaypayInfo byOutTradeNo = gatewaypayInfoRepository.findByOutTradeNo(out_trade_no);
                            logger.info("清分计算信息"+byOutTradeNo);
                            distribution(byOutTradeNo);
                        }
                    } else if ("FAIL".equals(status)) {
                        gatewaypayInfoRepository.updateFailTradeState(status, code, message, out_trade_no);
                    }
                    // 当上游订单交易状态发生改变的时候改变对账表对应的内容
                    sysReconInfoRepository.updateByOutTradeNo(trade_state , out_trade_no);
                    //向下发送通知
                    noticeService.ToDown(gatewaypayInfoRepository.findByOutTradeNo(out_trade_no));
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
        //创建一个map装返回信息
        Map<String,String> rsp = new HashMap();
        Gson gson = new Gson();
        GatewaypayInfo gatewaypayInfo = gson.fromJson(gatewaypayInfoToJson, GatewaypayInfo.class);
        String out_trade_no = gatewaypayInfo.getOut_trade_no();

        GatewaypayInfo finalGatewaypayInfo = gatewaypayInfoRepository.findByOutTradeNo(out_trade_no);
        if(null ==finalGatewaypayInfo){
            rsp.put("status", "FAIL");
            rsp.put("message", "查询信息错误");
            logger.error("未查到订单，查询错误");
            return gson.toJson(rsp);
        }
        return gson.toJson(finalGatewaypayInfo);
    }

    /**
     * 清分方法
     *
     * @param gatewaypayInfo
     */
    public void distribution(GatewaypayInfo gatewaypayInfo) {
        logger.info("清分计算信息："+gatewaypayInfo);
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
        logger.info("清分结果："+distributionInfo);

        SysReconciliationInfo sysReconciliationInfo = new SysReconciliationInfo();
        sysReconciliationInfo.setSp_id(gatewaypayInfo.getSp_id());
        sysReconciliationInfo.setTrade_time(gatewaypayInfo.getTrade_time());
        sysReconciliationInfo.setTrade_state(gatewaypayInfo.getTrade_state());
        sysReconciliationInfo.setTotal_fee(gatewaypayInfo.getTotal_fee());
        sysReconciliationInfo.setHand_fee(distributionInfo.getProfit());
        sysReconciliationInfo.setTrade_type("GATEWAY_PAY");
        sysReconciliationInfo.setSp_trade_no(gatewaypayInfo.getOut_trade_no());
        sysReconciliationInfo.setTrade_no(gatewaypayInfo.getCh_trade_no());
        sysReconciliationInfo.setDown_sp_id(gatewaypayInfo.getDown_sp_id());
        sysReconciliationInfo.setDown_mch_id(gatewaypayInfo.getDown_mch_id());

        // 保存系统对账信息
        sysReconInfoRepository.save(sysReconciliationInfo);
        //存数据库
        distributionInfoRespository.save(distributionInfo);
    }

}