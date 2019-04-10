package com.shangfudata.gatewaypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.gatewaypay.dao.*;
import com.shangfudata.gatewaypay.entity.DownSpInfo;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.entity.UpMchInfo;
import com.shangfudata.gatewaypay.entity.UpRoutingInfo;
import com.shangfudata.gatewaypay.eureka.EurekaGatewaypayClient;
import com.shangfudata.gatewaypay.service.GatewaypayService;
import com.shangfudata.gatewaypay.util.DataValidationUtils;
import com.shangfudata.gatewaypay.util.RSAUtils;
import com.shangfudata.gatewaypay.util.SignUtils;
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
public class GatewaypayServiceImpl implements GatewaypayService {

    String methodUrl = "http://192.168.88.65:8888/gate/gw/apply";

    @Autowired
    DownSpInfoRepository downSpInfoRepository;
    @Autowired
    GatewaypayInfoRepository gatewaypayInfoRepository;
    @Autowired
    EurekaGatewaypayClient eurekaGatewaypayClient;
    @Autowired
    UpRoutingInfoRepository upRoutingInfoRepository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;
    @Autowired
    UpMchInfoRepository upMchInfoRepository;

    Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 对下开放的网关交易
     *
     * @param gatewaypayInfoToJson
     * @return
     * @throws Exception
     */
    public String downGatewaypay(String gatewaypayInfoToJson) throws Exception {
        //创建一个map装返回信息
        Map rsp = new HashMap();
        //创建一个工具类对象
        DataValidationUtils dataValidationUtils = DataValidationUtils.builder();

        Gson gson = new Gson();

        Map map = gson.fromJson(gatewaypayInfoToJson, Map.class);

        //验空
        dataValidationUtils.isNullValid(map, rsp);
        if ("FAIL".equals(rsp.get("status"))) {
            return gson.toJson(rsp);
        }

        //取签名
        String sign = (String) map.remove("sign");
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        GatewaypayInfo gatewaypayInfo = gson.fromJson(gatewaypayInfoToJson, GatewaypayInfo.class);
        String down_sp_id = gatewaypayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRepository.findById(down_sp_id);
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
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)) {

            /* ------------------------ 路由分发 ------------------------------ */
            // 下游通道路由分发处理
            String downRoutingResponse = eurekaGatewaypayClient.downRouting(gatewaypayInfo.getDown_mch_id(), gatewaypayInfo.getDown_sp_id(), gatewaypayInfo.getTotal_fee(), "gatewaypay");
            Map downRoutingMap = gson.fromJson(downRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(downRoutingMap.get("status"))) {
                downRoutingMap.put("status", "FAIL");
                downRoutingMap.put("message", "上游没有可用通道");
                return gson.toJson(downRoutingMap);
            }

            // 根据 down_sp_id 查询路由表 , 获取 mch_id sp_id
            UpRoutingInfo upRoutingInfo = upRoutingInfoRepository.queryByDownSpId(gatewaypayInfo.getDown_sp_id(), "gatewaypay");

            // 如果为空返回无通道
            if (null == upRoutingInfo) {
                downRoutingMap.put("status", "FAIL");
                downRoutingMap.put("message", "上游没有可用通道");
                return gson.toJson(downRoutingMap);
            }

            // 查看 上游通道路由分发处理
            String upRoutingResponse = eurekaGatewaypayClient.upRouting(gatewaypayInfo.getDown_sp_id(), upRoutingInfo.getMch_id(), gatewaypayInfo.getTotal_fee(), "gatewaypay");
            Map upRoutingMap = gson.fromJson(upRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(upRoutingMap.get("status"))) {
                return gson.toJson(upRoutingMap);
            }
            /* ------------------------ 路由分发 ------------------------------ */

            String GatewaypayInfoToJson = gson.toJson(gatewaypayInfo);

            Map upGatewaypayInfoMap = gson.fromJson(GatewaypayInfoToJson, Map.class);
            upGatewaypayInfoMap.put("down_busi_id", downRoutingMap.get("down_busi_id"));
            upGatewaypayInfoMap.put("up_busi_id", upRoutingMap.get("up_busi_id"));
            upGatewaypayInfoMap.put("mch_id", upRoutingInfo.getMch_id());
            upGatewaypayInfoMap.put("sp_id", upRoutingInfo.getSp_id());
            String upEasypayInfoJson = gson.toJson(upGatewaypayInfoMap);

            //无异常，则调用上游交易方法
            logger.info("调用上游交易方法："+upEasypayInfoJson);
            return gatewaypayToUp(upEasypayInfoJson);
        }

        //验签失败，直接返回
        rsp.put("status", "FAIL");
        rsp.put("message", "签名错误");
        logger.error("签名错误...");
        return gson.toJson(rsp);
    }

    /**
     * 向上的网关交易
     *
     * @param gatewaypayInfoToJson
     * @return
     */
    public String gatewaypayToUp(String gatewaypayInfoToJson) {
        Gson gson = new Gson();
        Map gatewaypayInfoToMap = gson.fromJson(gatewaypayInfoToJson, Map.class);

        // 从 map 中删除并获取两个通道业务 id .
        String down_busi_id = (String) gatewaypayInfoToMap.remove("down_busi_id");
        String up_busi_id = (String) gatewaypayInfoToMap.remove("up_busi_id");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(gatewaypayInfoToMap);
        GatewaypayInfo gatewaypayInfo = gson.fromJson(s, GatewaypayInfo.class);

        //移除下游信息
        gatewaypayInfoToMap.remove("down_sp_id");
        gatewaypayInfoToMap.remove("down_mch_id");
        gatewaypayInfoToMap.remove("down_notify_url");
        gatewaypayInfoToMap.remove("sign");

        // 获取上游商户信息
        UpMchInfo upMchInfo = upMchInfoRepository.queryByMchId(gatewaypayInfo.getMch_id());
        //对上交易信息进行签名
        gatewaypayInfoToMap.put("sign", SignUtils.sign(gatewaypayInfoToMap, upMchInfo.getSign_key()));

        //发送请求
        logger.info("向上请求下单...");
        String responseInfo = HttpUtil.post(methodUrl, gatewaypayInfoToMap, 12000);
        if(null == responseInfo){
            logger.error("向上请求下单失败");
        }

        //判断是否为html代码块，
        boolean contains = responseInfo.contains("<html>");
        if (contains) {
            //正确响应，即html代码块，需要自设交易状态
            gatewaypayInfo.setStatus("SUCCESS");
            gatewaypayInfo.setTrade_state("NOTPAY");

            gatewaypayInfo.setDown_busi_id(down_busi_id);
            gatewaypayInfo.setUp_busi_id(up_busi_id);
            gatewaypayInfoRepository.save(gatewaypayInfo);

        } else {
            //错误响应，返回的是错误信息
            GatewaypayInfo response = gson.fromJson(responseInfo, GatewaypayInfo.class);

            gatewaypayInfo.setTrade_state(response.getStatus());
            gatewaypayInfo.setStatus(response.getStatus());
            gatewaypayInfo.setCode(response.getCode());
            gatewaypayInfo.setMessage(response.getMessage());

            gatewaypayInfoRepository.save(gatewaypayInfo);
        }

        // 无论正确还是错误都同步返回
        return responseInfo;
    }
}