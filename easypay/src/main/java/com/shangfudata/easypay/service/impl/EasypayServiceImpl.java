package com.shangfudata.easypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.easypay.dao.*;
import com.shangfudata.easypay.entity.DownSpInfo;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.entity.UpMchInfo;
import com.shangfudata.easypay.entity.UpRoutingInfo;
import com.shangfudata.easypay.eureka.EurekaEasypayClient;
import com.shangfudata.easypay.service.EasypayService;
import com.shangfudata.easypay.util.AesUtils;
import com.shangfudata.easypay.util.DataValidationUtils;
import com.shangfudata.easypay.util.RSAUtils;
import com.shangfudata.easypay.util.SignUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class EasypayServiceImpl implements EasypayService {

    @Autowired
    EasypayInfoRepository easypayInfoRepository;
    @Autowired
    DownSpInfoRepository downSpInfoRepository;
    @Autowired
    EurekaEasypayClient eurekaEasypayClient;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;
    @Autowired
    UpRoutingInfoRepository upRoutingInfoRepository;
    @Autowired
    UpMchInfoRepository upMchInfoRepository;

    String methodUrl = "http://192.168.88.65:8888/gate/epay/epapply";

    Logger logger = LoggerFactory.getLogger(this.getClass());

    //创建一个map装返回信息
    Map<String,String> rsp = new HashMap();

    /**
     * 对下开放快捷交易方法
     *
     * @param easypayInfoToJson
     * @return
     * @throws Exception
     */
    public String downEasypay(String easypayInfoToJson) {
        //创建一个工具类对象
        DataValidationUtils dataValidationUtils = DataValidationUtils.builder();
        Gson gson = new Gson();

        Map<String,String> jsonToMap = gson.fromJson(easypayInfoToJson, Map.class);

        //验空
        dataValidationUtils.isNullValid(jsonToMap, rsp);
        if ("FAIL".equals(rsp.get("status"))) {
            return gson.toJson(rsp);
        }

        //EasypayInfo easypayInfo = gson.fromJson(easypayInfoToJson, EasypayInfo.class);
        //String down_sp_id = easypayInfo.getDown_sp_id();

        //下游传递上来的机构id，签名信息
        String down_sp_id = jsonToMap.get("down_sp_id");
        DownSpInfo downSpInfo = downSpInfoRepository.findBySpId(down_sp_id);
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
        logger.info("解密后报文:"+decodeMap);
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
            String downRoutingResponse = eurekaEasypayClient.downRouting(decodeMap.get("down_mch_id"), decodeMap.get("down_sp_id"), decodeMap.get("total_fee"), "easypay");
            Map downRoutingMap = gson.fromJson(downRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(downRoutingMap.get("status"))) {
                return gson.toJson(downRoutingMap);
            }

            // 根据 down_sp_id 查询路由表 , 获取 mch_id sp_id
            UpRoutingInfo upRoutingInfo = upRoutingInfoRepository.queryByDownSpId(decodeMap.get("down_sp_id"), "easypay");

            // 如果为空返回无通道
            if (null == upRoutingInfo) {
                downRoutingMap.put("status", "FAIL");
                downRoutingMap.put("message", "上游没有可用通道");
                logger.error("上游没有可用通道");
                return gson.toJson(downRoutingMap);
            }

            // 查看 上游通道路由分发处理
            String upRoutingResponse = eurekaEasypayClient.upRouting(decodeMap.get("down_sp_id"), upRoutingInfo.getMch_id(), decodeMap.get("total_fee"), "easypay");
            Map upRoutingMap = gson.fromJson(upRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(upRoutingMap.get("status"))) {
                return gson.toJson(upRoutingMap);
            }
            /* ------------------------ 路由分发 ------------------------------ */

            Map upEasypayInfoMap = gson.fromJson(decodeJson, Map.class);
            upEasypayInfoMap.put("down_busi_id", downRoutingMap.get("down_busi_id"));
            upEasypayInfoMap.put("up_busi_id", upRoutingMap.get("up_busi_id"));
            upEasypayInfoMap.put("mch_id", upRoutingInfo.getMch_id());
            upEasypayInfoMap.put("sp_id", upRoutingInfo.getSp_id());
            String upEasypayInfoJson = gson.toJson(upEasypayInfoMap);

            //无异常，则调用上游交易方法
            logger.info("调用上游交易方法："+upEasypayInfoJson);
            return easypayToUp(upEasypayInfoJson);
        }

        //验签失败，直接返回
        rsp.put("status", "FAIL");
        rsp.put("message", "签名错误。。。");
        logger.error("签名错误...");
        return gson.toJson(rsp);
    }

    public String easypayToUp(String easypayInfoToJson) {
        Gson gson = new Gson();
        Map easypayInfoToMap = gson.fromJson(easypayInfoToJson, Map.class);

        // 从 map 中删除并获取两个通道业务 id .
        String down_busi_id = (String) easypayInfoToMap.remove("down_busi_id");
        String up_busi_id = (String) easypayInfoToMap.remove("up_busi_id");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(easypayInfoToMap);
        EasypayInfo easypayInfo = gson.fromJson(s, EasypayInfo.class);
        //移除下游信息
        String down_sp_id = (String)easypayInfoToMap.remove("down_sp_id");
        easypayInfoToMap.remove("down_mch_id");
        easypayInfoToMap.remove("down_notify_url");
        easypayInfoToMap.remove("sign");

        // 获取上游商户信息
        UpMchInfo upMchInfo = upMchInfoRepository.findByMchId(easypayInfo.getMch_id());
        //对上交易信息进行签名
        easypayInfoToMap.put("sign", SignUtils.sign(easypayInfoToMap, upMchInfo.getSign_key()));
        //AES加密操作
        upEncoding(easypayInfoToMap, upMchInfo.getSec_key());
        logger.info("AES加密后信息"+easypayInfoToMap);


        //发送请求
        logger.info("向上请求下单...");
        String responseInfo = HttpUtil.post(methodUrl, easypayInfoToMap, 12000);
        if(null == responseInfo){
            logger.error("向上请求下单失败");
        }


        //获取响应信息，并用一个新对象装下这些响应信息
        EasypayInfo response = gson.fromJson(responseInfo, EasypayInfo.class);
        //将响应信息存储到交易完整信息中
        easypayInfo.setTrade_state(response.getTrade_state());
        easypayInfo.setStatus(response.getStatus());
        easypayInfo.setCode(response.getCode());
        easypayInfo.setMessage(response.getMessage());
        easypayInfo.setCh_trade_no(response.getCh_trade_no());
        easypayInfo.setErr_code(response.getErr_code());
        easypayInfo.setErr_msg(response.getErr_msg());

        if ("SUCCESS".equals(response.getStatus())) {
            easypayInfo.setDown_busi_id(down_busi_id);
            easypayInfo.setUp_busi_id(up_busi_id);
            //将订单信息表存储数据库
            logger.error("上游处理成功信息："+responseInfo);
            easypayInfoRepository.save(easypayInfo);
        } else if ("FAIL".equals(response.getStatus())) {
            logger.error("上游处理失败信息："+responseInfo);
            easypayInfoRepository.save(easypayInfo);
            rsp.put("status", "FAIL");
            rsp.put("message", easypayInfo.getMessage());
            return gson.toJson(rsp);
        }

        DownSpInfo downSpInfo = downSpInfoRepository.findBySpId(down_sp_id);
        //拿到密钥(私钥)
        String my_pri_key = downSpInfo.getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = null;
        try {
            rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        } catch (Exception e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "密钥错误");
            logger.error("获取密钥错误:"+e);
            return gson.toJson(rsp);
        }

        // 封装响应数据
        rsp.put("status", "SUCCESS");
        rsp.put("out_trade_no",easypayInfo.getOut_trade_no());
        rsp.put("trade_state", "NOTPAY");
        rsp.put("err_code","TSP001");
        rsp.put("err_msg","等待交易");
        rsp.put("nonce_str", RandomStringUtils.randomAlphanumeric(10));
        rsp.put("sign",RSAUtils.sign(gson.toJson(rsp),rsaPrivateKey));
        return gson.toJson(rsp);
    }

    /**
     * RSA 解密方法
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
}