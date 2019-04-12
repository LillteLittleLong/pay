package com.shangfudata.distillpay.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.distillpay.dao.*;
import com.shangfudata.distillpay.entity.*;
import com.shangfudata.distillpay.eureka.EurekaDistillpayClient;
import com.shangfudata.distillpay.jms.DistillpaySenderService;
import com.shangfudata.distillpay.service.DistillpayService;
import com.shangfudata.distillpay.util.AesUtils;
import com.shangfudata.distillpay.util.DataValidationUtils;
import com.shangfudata.distillpay.util.RSAUtils;
import com.shangfudata.distillpay.util.SignUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DistillpayServiceImpl implements DistillpayService {

    String methodUrl = "http://testapi.shangfudata.com/gate/rtp/distillpay";

    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    DistillpayInfoRespository distillpayInfoRespository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;
    @Autowired
    DistributionInfoRepository distributionInfoRepository;
    @Autowired
    DistillpaySenderService distillpaySenderService;
    @Autowired
    UpRoutingInfoRepository upRoutingInfoRepository;
    @Autowired
    EurekaDistillpayClient eurekaDistillpayClient;
    @Autowired
    UpMchInfoRepository upMchInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;

    Logger logger = LoggerFactory.getLogger(this.getClass());


    public String downDistillpay(String distillpayInfoToJson) throws Exception{
        //创建一个map装返回信息
        Map rsp = new HashMap();
        //创建一个工具类对象
        DataValidationUtils dataValidationUtils = DataValidationUtils.builder();

        Gson gson = new Gson();

        Map map = gson.fromJson(distillpayInfoToJson, Map.class);

        //验空
        dataValidationUtils.isNullValid(map,rsp);
        if ("FAIL".equals(rsp.get("status"))) {
            return gson.toJson(rsp);
        }

        //取签名
        String sign = (String)map.remove("sign");
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        DistillpayInfo distillpayInfo = gson.fromJson(distillpayInfoToJson, DistillpayInfo.class);
        String down_sp_id = distillpayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);
        if(null == downSpInfo){
            rsp.put("status", "FAIL");
            rsp.put("message", "非法机构");
            logger.error("当前机构非法");
            return gson.toJson(rsp);
        }

        //拿到密钥(私钥)
        String my_pri_key = downSpInfo.get().getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = null;
        //拿到密钥(公钥)
        String down_pub_key = downSpInfo.get().getDown_pub_key();
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

        //公钥验签
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)){

            //私钥解密字段
            downDecoding(distillpayInfo, rsaPrivateKey,rsp);
            if ("FAIL".equals(rsp.get("status"))) {
                return gson.toJson(rsp);
            }

            // 数据效验
            dataValidationUtils.processMyException(distillpayInfo, rsp);
            if ("FAIL".equals(rsp.get("status"))) {
                return gson.toJson(rsp);
            }

            /* ------------------------ 路由分发 ------------------------------ */
            // 下游通道路由分发处理
            String downRoutingResponse = eurekaDistillpayClient.downRouting(distillpayInfo.getDown_mch_id(), distillpayInfo.getDown_sp_id(), distillpayInfo.getTotal_fee(), "distillpay");
            Map downRoutingMap = gson.fromJson(downRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(downRoutingMap.get("status"))) {
                return gson.toJson(downRoutingMap);
            }

            // 根据 down_sp_id 查询路由表 , 获取 mch_id sp_id
            UpRoutingInfo upRoutingInfo = upRoutingInfoRepository.queryByDownSpId(distillpayInfo.getDown_sp_id() , "distillpay");

            // 如果为空返回无通道
            if (null == upRoutingInfo) {
                downRoutingMap.put("status", "FAIL");
                downRoutingMap.put("message", "上游没有可用通道");
                return gson.toJson(downRoutingMap);
            }

            // 查看 上游通道路由分发处理
            String upRoutingResponse = eurekaDistillpayClient.upRouting(distillpayInfo.getDown_sp_id(), upRoutingInfo.getMch_id(), distillpayInfo.getTotal_fee(), "distillpay");
            Map upRoutingMap = gson.fromJson(upRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(upRoutingMap.get("status"))) {
                return gson.toJson(upRoutingMap);
            }
            /* ------------------------ 路由分发 ------------------------------ */

            // 无异常，保存下游请求信息到数据库
            distillpayInfoRespository.save(distillpayInfo);
            logger.info("保存下游请求信息到数据库："+distillpayInfo);

            // 包装参数
            String DistillpayInfoToJson = gson.toJson(distillpayInfo);

            Map upDistillpayInfoMap = gson.fromJson(DistillpayInfoToJson, Map.class);
            upDistillpayInfoMap.put("down_busi_id", downRoutingMap.get("down_busi_id"));
            upDistillpayInfoMap.put("up_busi_id", upRoutingMap.get("up_busi_id"));
            upDistillpayInfoMap.put("mch_id" , upRoutingInfo.getMch_id());
            upDistillpayInfoMap.put("sp_id" , upRoutingInfo.getSp_id());
            String upDistillpayInfoJson = gson.toJson(upDistillpayInfoMap);

            // 将信息发送到队列中
            distillpaySenderService.sendMessage("distillpayinfo.notice",upDistillpayInfoJson);

            // 封装响应数据
            rsp.put("sp_id",distillpayInfo.getDown_sp_id());
            rsp.put("mch_id",distillpayInfo.getDown_mch_id());
            rsp.put("status", "SUCCESS");
            rsp.put("trade_state", "正在处理中");

            //返回响应参数
            return gson.toJson(rsp);
        }

        //验签失败，直接返回
        rsp.put("status", "FAIL");
        rsp.put("message", "签名错误");
        logger.error("签名错误");
        return gson.toJson(rsp);
    }

    /**
     * 向上交易方法
     * @param distillpayInfoToJson
     * @return
     * 1.设置上游机构号和商户号
     * 2.删除下游机构号和商户号以及签名
     * 3.向上签名，加密，发送请求
     * 4.收到响应信息，存入传上来的collpay对象
     * 5.判断，保存数据库
     */
    @JmsListener(destination = "distillpayinfo.notice")
    public void distillpayToUp(String distillpayInfoToJson){
        logger.info("队列监听得到的消息："+distillpayInfoToJson);
        Gson gson = new Gson();
        Map distillpayInfoToMap = gson.fromJson(distillpayInfoToJson, Map.class);

        // 从 map 中删除并获取两个通道业务 id .
        String down_busi_id = (String) distillpayInfoToMap.remove("down_busi_id");
        String up_busi_id = (String) distillpayInfoToMap.remove("up_busi_id");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(distillpayInfoToMap);
        DistillpayInfo distillpayInfo = gson.fromJson(s,DistillpayInfo.class);

        //移除下游信息
        distillpayInfoToMap.remove("down_sp_id");
        distillpayInfoToMap.remove("down_mch_id");
        distillpayInfoToMap.remove("sign");
        distillpayInfoToMap.remove("notify_url");

        // 查询数据库获取上游商户加密解密信息
        UpMchInfo upMchInfo = upMchInfoRepository.queryByMchId(distillpayInfo.getMch_id());

        //对上交易信息进行签名
        distillpayInfoToMap.put("sign", SignUtils.sign(distillpayInfoToMap, upMchInfo.getSign_key()));
        //AES加密操作
        upEncoding(distillpayInfoToMap, upMchInfo.getSec_key());
        logger.info("AES加密后信息"+distillpayInfoToMap);

        //发送请求
        logger.info("向上请求交易...");
        String responseInfo = HttpUtil.post(methodUrl, distillpayInfoToMap, 12000);
        if(null == responseInfo){
            logger.error("向上请求交易失败");
        }
        logger.info("向上交易成功："+responseInfo);


        //获取响应信息，并用一个新DistillpayInfo对象装下这些响应信息
        DistillpayInfo response = gson.fromJson(responseInfo, DistillpayInfo.class);

        //将响应信息存储到当前downCollpayInfo及UpCollpayInfo请求交易完整信息中
        distillpayInfo.setTrade_state(response.getTrade_state());
        distillpayInfo.setStatus(response.getStatus());
        distillpayInfo.setCode(response.getCode());
        distillpayInfo.setMessage(response.getMessage());
        distillpayInfo.setCh_trade_no(response.getCh_trade_no());
        distillpayInfo.setErr_code(response.getErr_code());
        distillpayInfo.setErr_msg(response.getErr_msg());

        // 设置交易时间
        distillpayInfo.setTrade_time(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        if("SUCCESS".equals(response.getStatus())){
            logger.info("上游处理成功信息："+response);

            //将订单信息表存储数据库
            distillpayInfo.setDown_busi_id(down_busi_id);
            distillpayInfo.setUp_busi_id(up_busi_id);
            distillpayInfoRespository.save(distillpayInfo);

            // 清分参数包装
            Map<String, String> distributionMap = new HashMap<>();
            distributionMap.put("up_busi_id", up_busi_id);
            distributionMap.put("down_busi_id", down_busi_id);
            distributionMap.put("out_trade_no", distillpayInfo.getOut_trade_no());
            //清分
            distribution(distributionMap);
        }else if("FAIL".equals(response.getStatus())){
            logger.info("上游处理失败信息："+response);
            distillpayInfoRespository.save(distillpayInfo);
        }
    }

    /**
     * RSA 解密方法
     *
     * @param distillpayInfo
     * @param rsaPrivateKey
     */
    public void downDecoding(DistillpayInfo distillpayInfo, RSAPrivateKey rsaPrivateKey,Map rsp)  {
        try {
            distillpayInfo.setCard_name(RSAUtils.privateKeyDecrypt(distillpayInfo.getCard_name(), rsaPrivateKey));
            distillpayInfo.setCard_no(RSAUtils.privateKeyDecrypt(distillpayInfo.getCard_no(), rsaPrivateKey));
            distillpayInfo.setId_no(RSAUtils.privateKeyDecrypt(distillpayInfo.getId_no(), rsaPrivateKey));
        } catch (Exception e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "密钥错误");
            logger.error("RSA解密exception:"+e);
        }
    }

    /**
     * AES 加密方法
     *
     * @param map
     * @param aesKey
     */
    public void upEncoding(Map map, String aesKey) {
        map.replace("card_name", AesUtils.aesEn((String) map.get("card_name"), aesKey));
        map.replace("card_no", AesUtils.aesEn((String) map.get("card_no"), aesKey));
        map.replace("id_no", AesUtils.aesEn((String) map.get("id_no"), aesKey));
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
        sysReconciliationInfo.setHand_fee(distributionInfo.getUp_charge());
        sysReconciliationInfo.setTrade_type("DISTILL_PAY");
        sysReconciliationInfo.setSp_trade_no(byOutTradeNo.getOut_trade_no());
        sysReconciliationInfo.setTrade_no(byOutTradeNo.getCh_trade_no());
        sysReconciliationInfo.setDown_sp_id(byOutTradeNo.getDown_sp_id());
        sysReconciliationInfo.setDown_mch_id(byOutTradeNo.getDown_mch_id());
        sysReconciliationInfo.setDown_charge(distributionInfo.getDown_charge());

        // 保存系统对账信息
        sysReconInfoRepository.save(sysReconciliationInfo);
        //存数据库
        distributionInfoRepository.save(distributionInfo);
    }

}
