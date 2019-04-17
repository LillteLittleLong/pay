package com.shangfudata.distillpay.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;

import com.shangfudata.distillpay.dao.*;
import com.shangfudata.distillpay.entity.*;
import com.shangfudata.distillpay.service.NoticeService;
import com.shangfudata.distillpay.service.QueryService;
import com.shangfudata.distillpay.util.RSAUtils;
import com.shangfudata.distillpay.util.SignUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {

    String methodUrl = "http://testapi.shangfudata.com/gate/spsvr/order/qry";

    @Autowired
    NoticeService noticeService;
    @Autowired
    DistillpayInfoRespository distillpayInfoRespository;
    @Autowired
    UpMchInfoRepository upMchInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;
    @Autowired
    DistributionInfoRepository distributionInfoRepository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 向上查询（轮询方法）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void queryToUp() {
        Gson gson = new Gson();

        //查询所有交易状态为PROCESSING的订单信息
        List<DistillpayInfo> distillpayInfoList = distillpayInfoRespository.findByTradeState("PROCESSING");

        //遍历
        for (DistillpayInfo distillpayInfo : distillpayInfoList) {
            //判断处理状态为SUCCESS的才进行下一步操作
            if ("SUCCESS".equals(distillpayInfo.getStatus())) {
                //查询参数对象
                QueryInfo queryInfo = new QueryInfo();
                queryInfo.setMch_id(distillpayInfo.getMch_id());
                queryInfo.setNonce_str(distillpayInfo.getNonce_str());
                queryInfo.setOut_trade_no(distillpayInfo.getOut_trade_no());

                //获取上游商户信息
                UpMchInfo upMchInfo = upMchInfoRepository.queryByMchId(distillpayInfo.getMch_id());

                //将queryInfo转为json，再转map
                String query = gson.toJson(queryInfo);
                Map queryMap = gson.fromJson(query, Map.class);
                //签名
                queryMap.put("sign", SignUtils.sign(queryMap, upMchInfo.getSign_key()));
                logger.info("向上查询请求信息："+queryMap);

                //发送查询请求，得到响应信息
                String queryResponse = HttpUtil.post(methodUrl, queryMap, 6000);
                if(null == queryResponse){
                    logger.error("向上查询请求失败：");
                }else{
                    logger.info("向上查询请求响应信息："+queryResponse);
                }

                //使用一个新的UpdistillpayInfo对象，接收响应参数
                DistillpayInfo responseInfo = gson.fromJson(queryResponse, DistillpayInfo.class);
                //如果交易状态发生改变，那就更新。
                if (!(responseInfo.getTrade_state().equals(distillpayInfo.getTrade_state()))) {
                    //得到订单号
                    String out_trade_no = distillpayInfo.getOut_trade_no();
                    String status = responseInfo.getStatus();
                    //成功信息
                    String trade_state = responseInfo.getTrade_state();
                    String err_code = responseInfo.getErr_code();
                    String err_msg = responseInfo.getErr_msg();
                    //失败信息
                    String code = responseInfo.getCode();
                    String message = responseInfo.getMessage();

                    // 获取查询请求
                    //System.out.println("查询请求内容 > " + queryResponse);
                    if ("SUCCESS".equals(status)) {
                        // 清分参数包装
                        Map<String, String> distributionMap = new HashMap<>();
                        distributionMap.put("up_busi_id", distillpayInfo.getUp_busi_id());
                        distributionMap.put("down_busi_id",distillpayInfo.getDown_busi_id());
                        distributionMap.put("out_trade_no", distillpayInfo.getOut_trade_no());
                        //清分
                        distribution(distributionMap);

                        logger.info("交易成功："+queryResponse);
                        //将订单信息表存储数据库
                        distillpayInfoRespository.updateSuccessTradeState(trade_state, err_code, err_msg, out_trade_no);
                    } else if ("FAIL".equals(status)) {
                        logger.info("交易失败："+queryResponse);
                        distillpayInfoRespository.updateFailTradeState(status, code, message, out_trade_no);
                    }
                    // 当上游订单交易状态发生改变的时候改变对账表对应的内容
                    sysReconInfoRepository.updateByOutTradeNo(trade_state , out_trade_no);
                    //发送通知
                    noticeService.notice(distillpayInfoRespository.findByOutTradeNo(out_trade_no));
                }
            }
        }
    }

    /**
     * 下游查询方法
     *
     * @param distillpayInfoToJson
     */
    public String downQuery(String distillpayInfoToJson) {
        //创建一个map装返回信息
        Map<String,String> rsp = new HashMap();

        Gson gson = new Gson();

        Map<String,String> jsonToMap = gson.fromJson(distillpayInfoToJson, Map.class);

        String sign = jsonToMap.remove("sign");
        String down_sp_id = jsonToMap.get("down_sp_id");
        DownSpInfo downSpInfo = downSpInfoRespository.findBySpId(down_sp_id);

        //拿到密钥(私钥)
        String my_pri_key = downSpInfo.getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = null;
        //拿到密钥(公钥)
        String down_pub_key = downSpInfo.getDown_pub_key();
        RSAPublicKey rsaPublicKey = null;
        try {
            rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
            rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);
        } catch (Exception e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "密钥错误");
            logger.error("获取密钥错误:"+e);
            return gson.toJson(rsp);
        }

        //验签
        if (RSAUtils.doCheck(gson.toJson(jsonToMap), sign, rsaPublicKey)) {
            //随机字符串验证
            String nonce_str = jsonToMap.get("nonce_str");
            if (!(nonce_str.length() == 32)) {
                rsp.put("status", "FAIL");
                rsp.put("message", "[nonce_str]随机字符串长度错误");
                logger.error("随机字符串长度错误");
                return gson.toJson(rsp);
            }

            DistillpayInfo finalDistillpayInfo = distillpayInfoRespository.findByOutTradeNo(jsonToMap.get("out_trade_no"));

            if(null == finalDistillpayInfo){
                rsp.put("status", "FAIL");
                rsp.put("message", "查询信息错误");
                logger.error("未查到订单，查询错误");
                return gson.toJson(rsp);
            }
            rsp.put("status","SUCCESS");
            rsp.put("out_trade_no",finalDistillpayInfo.getOut_trade_no());
            rsp.put("trade_state",finalDistillpayInfo.getTrade_state());
            rsp.put("err_code",finalDistillpayInfo.getErr_code());
            rsp.put("err_msg",finalDistillpayInfo.getErr_msg());
            rsp.put("nonce_str", RandomStringUtils.randomAlphanumeric(10));
            rsp.put("sign",RSAUtils.sign(gson.toJson(rsp),rsaPrivateKey));
            return gson.toJson(rsp);
        }

        //验签失败，直接返回
        rsp.put("status", "FAIL");
        rsp.put("message", "[sign]签名错误");
        logger.error("签名错误");
        return gson.toJson(rsp);



        /*Gson gson = new Gson();
        DistillpayInfo distillpayInfo = gson.fromJson(distillpayInfoToJson, DistillpayInfo.class);
        String out_trade_no = distillpayInfo.getOut_trade_no();

        DistillpayInfo finalDistillpayInfo = distillpayInfoRespository.findByOutTradeNo(out_trade_no);

        return gson.toJson(finalDistillpayInfo);*/
    }



    /**
     * 清分方法
     * @param distillpayInfoMap
     */
    public void distribution(Map<String, String> distillpayInfoMap) {
        logger.info("清分计算信息："+distillpayInfoMap);
        //通过路由给的上下游两个商户业务 id 查询数据库 .
        DownMchBusiInfo downMchBusiInfo = downMchBusiInfoRepository.getOne(distillpayInfoMap.get("down_busi_id"));
        UpMchBusiInfo upMchBusiInfo = upMchBusiInfoRepository.getOne(distillpayInfoMap.get("up_busi_id"));

        // 根据订单号获取订单信息
        String out_trade_no = distillpayInfoMap.get("out_trade_no");
        DistillpayInfo byOutTradeNo = distillpayInfoRespository.findByOutTradeNo(out_trade_no);

        //交易金额
        BigDecimal Total_fee = Convert.toBigDecimal(byOutTradeNo.getTotal_fee());
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
        distributionInfo.setOut_trade_no(byOutTradeNo.getOut_trade_no());
        distributionInfo.setBusi_type("distillpay");
        distributionInfo.setDown_mch_id(byOutTradeNo.getDown_mch_id());
        distributionInfo.setDown_charge(final_down_charge.toString());
        distributionInfo.setUp_mch_id(byOutTradeNo.getMch_id());
        distributionInfo.setUp_charge(final_up_charge.toString());
        distributionInfo.setProfit(profit.toString());
        distributionInfo.setTrad_amount(Total_fee.toString());
        logger.info("清分结果："+distributionInfo);

        SysReconciliationInfo sysReconciliationInfo = new SysReconciliationInfo();
        sysReconciliationInfo.setSp_id(byOutTradeNo.getSp_id());
        sysReconciliationInfo.setTrade_time(byOutTradeNo.getTrade_time());
        sysReconciliationInfo.setTrade_state(byOutTradeNo.getTrade_state());
        sysReconciliationInfo.setTotal_fee(byOutTradeNo.getTotal_fee());
        sysReconciliationInfo.setHand_fee(distributionInfo.getProfit());
        sysReconciliationInfo.setTrade_type("DISTILL_PAY");
        sysReconciliationInfo.setSp_trade_no(byOutTradeNo.getOut_trade_no());
        sysReconciliationInfo.setTrade_no(byOutTradeNo.getCh_trade_no());
        sysReconciliationInfo.setDown_sp_id(byOutTradeNo.getDown_sp_id());
        sysReconciliationInfo.setDown_mch_id(byOutTradeNo.getDown_mch_id());

        // 保存系统对账信息
        sysReconInfoRepository.save(sysReconciliationInfo);
        //存数据库
        distributionInfoRepository.save(distributionInfo);
    }
}
