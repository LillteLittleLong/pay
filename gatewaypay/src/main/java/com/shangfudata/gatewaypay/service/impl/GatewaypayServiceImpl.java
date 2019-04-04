package com.shangfudata.gatewaypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.gatewaypay.dao.*;
import com.shangfudata.gatewaypay.entity.DownSpInfo;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.entity.UpRoutingInfo;
import com.shangfudata.gatewaypay.eureka.EurekaGatewaypayClient;
import com.shangfudata.gatewaypay.service.GatewaypayService;
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
    DownSpInfoRepository downSpInfoRespository;
    @Autowired
    GatewaypayInfoRepository gatewaypayInfoRespository;
    @Autowired
    EurekaGatewaypayClient eurekaGatewaypayClient;
    @Autowired
    UpRoutingInfoRepository upRoutingInfoRepository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;

    /**
     * 对下开放的网关交易
     *
     * @param gatewaypayInfoToJson
     * @return
     * @throws Exception
     */
    public String downGatewaypay(String gatewaypayInfoToJson) throws Exception {
        Map<String , String> responseMap = new HashMap();
        Gson gson = new Gson();

        Map map = gson.fromJson(gatewaypayInfoToJson, Map.class);
        System.out.println("下游请求参数 " + map);
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
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)) {
            // TODO: 2019/4/3 数据效验

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

            // 保存订单信息到数据库
            gatewaypayInfoRespository.save(gatewaypayInfo);
            // 包装上游请求参数
            String GatewaypayInfoToJson = gson.toJson(gatewaypayInfo);
            Map upGatewaypayInfoMap = gson.fromJson(GatewaypayInfoToJson, Map.class);
            upGatewaypayInfoMap.put("down_busi_id", downRoutingMap.get("down_busi_id"));
            upGatewaypayInfoMap.put("up_busi_id", upRoutingMap.get("up_busi_id"));
            upGatewaypayInfoMap.put("mch_id", upRoutingInfo.getMch_id());
            upGatewaypayInfoMap.put("sp_id", upRoutingInfo.getSp_id());
            String upGatewaypayInfoJson = gson.toJson(upGatewaypayInfoMap);

            // 执行上游请求参数
            return gatewaypayToUp(upGatewaypayInfoJson);
        }
        //验签失败，直接返回
        responseMap.put("status", "FAIL");
        responseMap.put("message", "签名错误");
        return gson.toJson(responseMap);
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
        gatewaypayInfoToMap.remove("sign");
        //对上交易信息进行签名
        gatewaypayInfoToMap.put("sign", SignUtils.sign(gatewaypayInfoToMap, signKey));
        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, gatewaypayInfoToMap, 12000);

        boolean contains = responseInfo.contains("<html>");
        if (contains) {
            // 设置为空参数
            gatewaypayInfo.setStatus("SUCCESS");
            gatewaypayInfo.setTrade_state("PROCESSING");
            // 设置上游 下游业务通道
            gatewaypayInfo.setDown_busi_id(down_busi_id);
            gatewaypayInfo.setUp_busi_id(up_busi_id);

            gatewaypayInfoRespository.save(gatewaypayInfo);
        } else {
            GatewaypayInfo response = gson.fromJson(responseInfo, GatewaypayInfo.class);
            gatewaypayInfo.setTrade_state(response.getStatus());
            gatewaypayInfo.setStatus(response.getStatus());
            gatewaypayInfo.setCode(response.getCode());
            gatewaypayInfo.setMessage(response.getMessage());
            gatewaypayInfoRespository.save(gatewaypayInfo);
        }
        return responseInfo;
    }

}