package com.shangfudata.gatewaypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;

import com.shangfudata.gatewaypay.dao.DownSpInfoRespository;
import com.shangfudata.gatewaypay.dao.GatewaypayInfoRespository;
import com.shangfudata.gatewaypay.entity.DownSpInfo;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.service.GatewaypayService;
import com.shangfudata.gatewaypay.util.DataValidationUtils;
import com.shangfudata.gatewaypay.util.RSAUtils;
import com.shangfudata.gatewaypay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Service
public class GatewaypayServiceImpl implements GatewaypayService {

    String methodUrl = "http://192.168.88.65:8888/gate/gw/apply";
    String signKey = "00000000000000000000000000000000";

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    @Autowired
    GatewaypayInfoRespository gatewaypayInfoRespository;

    /**
     * 对下开放的网关交易
     * @param gatewaypayInfoToJson
     * @return
     * @throws Exception
     */
    public String downGatewaypay(String gatewaypayInfoToJson) throws Exception {

        //创建一个map装返回信息
        Map responseMap = new HashMap();
        //创建一个工具类对象
        DataValidationUtils dataValidationUtils = DataValidationUtils.builder();

        Gson gson = new Gson();

        Map map = gson.fromJson(gatewaypayInfoToJson, Map.class);

        //验空
        String message = dataValidationUtils.isNullValid(map);
        if (!(message.equals(""))) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", message);
            return gson.toJson(responseMap);
        }

        //取签名
        String sign = (String)map.remove("sign");
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        GatewaypayInfo gatewaypayInfo = gson.fromJson(gatewaypayInfoToJson, GatewaypayInfo.class);
        String down_sp_id = gatewaypayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);
        //拿到我自己（平台）的密钥(私钥)
        String my_pri_key = downSpInfo.get().getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        //拿到下游给的密钥(公钥)
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //公钥验签
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)){

            // 异常处理
            dataValidationUtils.processMyException(gatewaypayInfo , responseMap);

            // 异常处理后判断是否需要返回
            if("FAIL".equals(responseMap.get("status"))){
                return gson.toJson(responseMap);
            }

            // 无异常，调用向上交易方法
           return gatewaypayToUp(gson.toJson(gatewaypayInfo));

        }

        //验签失败，直接返回
        responseMap.put("status", "FAIL");
        responseMap.put("message", "签名错误");
        return gson.toJson(responseMap);

    }

    /**
     * 向上的网关交易
     * @param gatewaypayInfoToJson
     * @return
     */
    public String gatewaypayToUp(String gatewaypayInfoToJson) {
        Gson gson = new Gson();

        Map gatewaypayInfoToMap = gson.fromJson(gatewaypayInfoToJson, Map.class);

        System.out.println("：："+gatewaypayInfoToJson);

        //设置上游服务商号及机构号
        gatewaypayInfoToMap.put("sp_id","1000");
        gatewaypayInfoToMap.put("mch_id","100001000000000001");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(gatewaypayInfoToMap);
        GatewaypayInfo gatewaypayInfo = gson.fromJson(s,GatewaypayInfo.class);

        //移除下游信息
        gatewaypayInfoToMap.remove("down_sp_id");
        gatewaypayInfoToMap.remove("down_mch_id");
        gatewaypayInfoToMap.remove("down_notify_url");
        gatewaypayInfoToMap.remove("sign");

        //对上交易信息进行签名
        gatewaypayInfoToMap.put("sign", SignUtils.sign(gatewaypayInfoToMap, signKey));

        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, gatewaypayInfoToMap, 12000);

        //判断是否为html代码块，
        boolean contains = responseInfo.contains("<html>");
        if(contains){
            //正确响应，即html代码块，需要自设交易状态
            gatewaypayInfo.setStatus("SUCCESS");
            gatewaypayInfo.setTrade_state("NOTPAY");
            gatewaypayInfoRespository.save(gatewaypayInfo);

        }else{
            //错误响应，返回的是错误信息
            GatewaypayInfo response = gson.fromJson(responseInfo, GatewaypayInfo.class);

            gatewaypayInfo.setTrade_state(response.getStatus());
            gatewaypayInfo.setStatus(response.getStatus());
            gatewaypayInfo.setCode(response.getCode());
            gatewaypayInfo.setMessage(response.getMessage());

            gatewaypayInfoRespository.save(gatewaypayInfo);
        }

        //无论正确还是错误都同步返回
        return responseInfo;
    }





}
