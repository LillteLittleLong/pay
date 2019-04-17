package com.shangfudata.collpay.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.collpay.dao.*;
import com.shangfudata.collpay.entity.*;
import com.shangfudata.collpay.eureka.EurekaCollpayClient;
import com.shangfudata.collpay.jms.CollpaySenderService;
import com.shangfudata.collpay.service.CollpayService;
import com.shangfudata.collpay.service.NoticeService;
import com.shangfudata.collpay.util.AesUtils;
import com.shangfudata.collpay.util.DataValidationUtils;
import com.shangfudata.collpay.util.RSAUtils;
import com.shangfudata.collpay.util.SignUtils;
import org.apache.commons.lang.RandomStringUtils;
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
import java.util.Random;

/**
 * 交易接口
 */
@Service
public class CollpayServiceImpl implements CollpayService {

    @Autowired
    CollpayInfoRespository collpayInfoRespository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    CollpaySenderService collpaySenderService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    EurekaCollpayClient eurekaCollpayClient;
    @Autowired
    UpMchInfoRepository upMchInfoRepository;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;
    @Autowired
    DistributionInfoRespository distributionInfoRespository;
    @Autowired
    UpRoutingInfoRepository upRoutingInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;

    private String methodUrl = "http://testapi.shangfudata.com/gate/cp/collpay";

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 交易方法
     * 1.下游传递一个json,获取其中的下游机构号以及签名
     * 2.调用查询方法，获取当前商户的密钥
     * 3.进行验签，字段解密，获取明文、
     * 4.调用向上交易请求方法，参数为CollpayInfoToJson对象
     */
    public String downCollpay(String CollpayInfoToJson) {
        //创建一个map装返回信息
        Map<String,String> rsp = new HashMap();
        //创建一个工具类对象
        DataValidationUtils dataValidationUtils = DataValidationUtils.builder();
        Gson gson = new Gson();

        Map<String,String> jsonToMap = gson.fromJson(CollpayInfoToJson, Map.class);

        //验空
        dataValidationUtils.isNullValid(jsonToMap,rsp);
        if ("FAIL".equals(rsp.get("status"))) {
            return gson.toJson(rsp);
        }

        //下游传递上来的机构id，签名信息
        String down_sp_id = jsonToMap.get("down_sp_id");
        DownSpInfo downSpInfo = downSpInfoRespository.findBySpId(down_sp_id);
        logger.info("下游机构信息：："+downSpInfo);
        if(null == downSpInfo){
            rsp.put("status", "FAIL");
            rsp.put("message", "非法机构");
            logger.error("当前机构非法");
            return gson.toJson(rsp);
        }

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

        //私钥解密字段
        Map<String, String> decodeMap = downDecoding(jsonToMap, rsaPrivateKey, rsp);
        if ("FAIL".equals(rsp.get("status"))) {
            return gson.toJson(rsp);
        }

        //取签名
        String sign = decodeMap.remove("sign");
        String decodeJson = gson.toJson(decodeMap);
        //公钥验签
        if (RSAUtils.doCheck(decodeJson, sign, rsaPublicKey)) {

            // 数据效验
            dataValidationUtils.processMyException(decodeMap, rsp);
            if ("FAIL".equals(rsp.get("status"))) {
                return gson.toJson(rsp);
            }

            /* ------------------------ 路由分发 ------------------------------ */
            // 下游通道路由分发处理
            String downRoutingResponse = eurekaCollpayClient.downRouting(decodeMap.get("down_mch_id"), decodeMap.get("down_sp_id"), decodeMap.get("total_fee"), "collpay");
            Map downRoutingMap = gson.fromJson(downRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(downRoutingMap.get("status"))) {
                return gson.toJson(downRoutingMap);
            }

            // 根据 down_sp_id 查询路由表 , 获取 mch_id sp_id
            UpRoutingInfo upRoutingInfo = upRoutingInfoRepository.queryByDownSpId(decodeMap.get("down_sp_id"), "collpay");

            // 如果为空返回无通道
            if (null == upRoutingInfo) {
                downRoutingMap.put("status", "FAIL");
                downRoutingMap.put("message", "上游没有可用通道");
                logger.error("上游没有可用通道");
                return gson.toJson(downRoutingMap);
            }

            // 查看 上游通道路由分发处理
            String upRoutingResponse = eurekaCollpayClient.upRouting(decodeMap.get("down_sp_id"), upRoutingInfo.getMch_id(), decodeMap.get("total_fee"), "collpay");
            Map upRoutingMap = gson.fromJson(upRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(upRoutingMap.get("status"))) {
                return gson.toJson(upRoutingMap);
            }
            /* ------------------------ 路由分发 ------------------------------ */

            // 无异常，保存下游请求信息到数据库
            CollpayInfo collpayInfo = gson.fromJson(decodeJson, CollpayInfo.class);
            collpayInfoRespository.save(collpayInfo);
            logger.info("保存下游请求信息到数据库："+collpayInfo);

            // 包装参数
            String collpayInfoToJson = gson.toJson(collpayInfo);

            Map upCollpayInfoMap = gson.fromJson(collpayInfoToJson, Map.class);
            upCollpayInfoMap.put("down_busi_id", downRoutingMap.get("down_busi_id"));
            upCollpayInfoMap.put("up_busi_id", upRoutingMap.get("up_busi_id"));
            upCollpayInfoMap.put("mch_id", upRoutingInfo.getMch_id());
            upCollpayInfoMap.put("sp_id", upRoutingInfo.getSp_id());
            String upCollpayInfoJson = gson.toJson(upCollpayInfoMap);

            collpaySenderService.sendMessage("collpayinfo.notice", upCollpayInfoJson);

            // 封装响应数据
            rsp.put("out_trade_no",collpayInfo.getOut_trade_no());
            rsp.put("status", "SUCCESS");
            rsp.put("trade_state", "PROCESSING");
            rsp.put("err_code","TSP001");
            rsp.put("err_msg","正在处理中");
            rsp.put("nonce_str", RandomStringUtils.randomAlphanumeric(10));
            rsp.put("sign",RSAUtils.sign(gson.toJson(rsp),rsaPrivateKey));

            //返回响应参数
            return gson.toJson(rsp);
        }
        //验签失败，直接返回
        rsp.put("status", "FAIL");
        rsp.put("message", "[sign]签名错误");
        logger.error("签名错误");
        return gson.toJson(rsp);
    }

    /**
     * 向上交易方法
     * 1.设置上游机构号和商户号
     * 2.删除下游机构号和商户号以及签名
     * 3.向上签名，加密，发送请求
     * 4.收到响应信息，存入传上来的collpay对象
     * 5.判断，保存数据库
     *
     * @param collpayInfoToJson
     */
    @JmsListener(destination = "collpayinfo.notice")
    public void collpayToUp(String collpayInfoToJson) {
        logger.info("队列监听得到的消息："+collpayInfoToJson);
        Gson gson = new Gson();
        Map collpayInfoToMap = gson.fromJson(collpayInfoToJson, Map.class);

        // 从 map 中删除并获取两个通道业务 id .
        String down_busi_id = (String) collpayInfoToMap.remove("down_busi_id");
        String up_busi_id = (String) collpayInfoToMap.remove("up_busi_id");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(collpayInfoToMap);
        CollpayInfo collpayInfo = gson.fromJson(s, CollpayInfo.class);

        //移除下游信息
        collpayInfoToMap.remove("down_sp_id");
        collpayInfoToMap.remove("down_mch_id");
        collpayInfoToMap.remove("sign");

        // 查询数据库获取上游商户加密解密信息
        UpMchInfo upMchInfo = upMchInfoRepository.queryByMchId(collpayInfo.getMch_id());

        //对上交易信息进行签名
        collpayInfoToMap.put("sign", SignUtils.sign(collpayInfoToMap, upMchInfo.getSign_key()));
        //AES加密操作
        upEncoding(collpayInfoToMap, upMchInfo.getSec_key());
        logger.info("AES加密后信息"+collpayInfoToMap);

        //发送请求
        logger.info("向上请求交易...");
        String responseInfo = HttpUtil.post(methodUrl, collpayInfoToMap, 12000);
        if(null == responseInfo){
            logger.error("向上请求交易失败");
        }
        logger.info("向上交易成功："+responseInfo);


        //获取响应信息，并用一个新CollpayInfo对象装下这些响应信息
        CollpayInfo response = gson.fromJson(responseInfo, CollpayInfo.class);

        //将响应信息存储到当前downCollpayInfo及UpCollpayInfo请求交易完整信息中
        collpayInfo.setTrade_state(response.getTrade_state());
        collpayInfo.setStatus(response.getStatus());
        collpayInfo.setCode(response.getCode());
        collpayInfo.setMessage(response.getMessage());
        collpayInfo.setCh_trade_no(response.getCh_trade_no());
        collpayInfo.setErr_code(response.getErr_code());
        collpayInfo.setErr_msg(response.getErr_msg());

        // 设置交易时间
        collpayInfo.setTrade_time(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        if ("SUCCESS".equals(response.getStatus())) {
            //将订单信息表存储数据库
            logger.info("上游处理成功信息："+response);
            collpayInfo.setDown_busi_id(down_busi_id);
            collpayInfo.setUp_busi_id(up_busi_id);
            collpayInfoRespository.save(collpayInfo);

            // 清分参数包装
            Map<String, String> distributionMap = new HashMap<>();
            distributionMap.put("up_busi_id", up_busi_id);
            distributionMap.put("down_busi_id", down_busi_id);
            distributionMap.put("out_trade_no", collpayInfo.getOut_trade_no());
            // 清分
            distribution(distributionMap);
        } else if ("FAIL".equals(response.getStatus())) {
            logger.info("上游处理失败信息："+response);
            collpayInfoRespository.save(collpayInfo);
        }

        // 向下游发送通知,参数为  当前订单
        noticeService.notice(collpayInfoRespository.findByOutTradeNo(collpayInfo.getOut_trade_no()));
    }

    /**
     * RSA 解密方法
     *
     * @param map
     * @param rsaPrivateKey
     */
    public Map<String,String> downDecoding(Map<String,String> map, RSAPrivateKey rsaPrivateKey,Map rsp)  {

        try {
            map.put("card_name",RSAUtils.privateKeyDecrypt(map.get("card_name"), rsaPrivateKey));
            map.put("card_no",RSAUtils.privateKeyDecrypt(map.get("card_no"), rsaPrivateKey));
            map.put("id_no",RSAUtils.privateKeyDecrypt(map.get("id_no"), rsaPrivateKey));
            map.put("bank_mobile",RSAUtils.privateKeyDecrypt(map.get("bank_mobile"), rsaPrivateKey));
            map.put("cvv2",RSAUtils.privateKeyDecrypt(map.get("cvv2"), rsaPrivateKey));
            map.put("card_valid_date",RSAUtils.privateKeyDecrypt(map.get("card_valid_date"), rsaPrivateKey));

        } catch (Exception e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "密钥错误");
            logger.error("RSA解密exception:"+e);
            return rsp;
        }
        return map;


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
        map.replace("cvv2", AesUtils.aesEn((String) map.get("cvv2"), aesKey));
        map.replace("card_valid_date", AesUtils.aesEn((String) map.get("card_valid_date"), aesKey));
        map.replace("bank_mobile", AesUtils.aesEn((String) map.get("bank_mobile"), aesKey));
    }

    /**
     * 清分方法
     *
     * @param collpayInfoMap
     */
    public void distribution(Map<String, String> collpayInfoMap) {
        logger.info("清分计算信息："+collpayInfoMap);
        //通过路由给的上下游两个商户业务 id 查询数据库 .
        DownMchBusiInfo downMchBusiInfo = downMchBusiInfoRepository.getOne(collpayInfoMap.get("down_busi_id"));
        UpMchBusiInfo upMchBusiInfo = upMchBusiInfoRepository.getOne(collpayInfoMap.get("up_busi_id"));

        // 根据订单号获取订单信息
        String out_trade_no = collpayInfoMap.get("out_trade_no");
        CollpayInfo byOutTradeNo = collpayInfoRespository.findByOutTradeNo(out_trade_no);

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
        distributionInfo.setBusi_type("collpay");
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
        sysReconciliationInfo.setTrade_type("CP_PAY");
        sysReconciliationInfo.setSp_trade_no(byOutTradeNo.getOut_trade_no());
        sysReconciliationInfo.setTrade_no(byOutTradeNo.getCh_trade_no());
        sysReconciliationInfo.setDown_sp_id(byOutTradeNo.getDown_sp_id());
        sysReconciliationInfo.setDown_mch_id(byOutTradeNo.getDown_mch_id());

        // 保存系统对账信息
        sysReconInfoRepository.save(sysReconciliationInfo);
        //存数据库
        distributionInfoRespository.save(distributionInfo);
    }
}