package com.shangfudata.gatewaypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;

import com.shangfudata.gatewaypay.dao.DownSpInfoRepository;
import com.shangfudata.gatewaypay.dao.GatewaypayInfoRepository;
import com.shangfudata.gatewaypay.entity.DownSpInfo;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.service.NoticeService;
import com.shangfudata.gatewaypay.util.RSAUtils;
import org.apache.activemq.command.ActiveMQQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    GatewaypayInfoRepository gatewaypayInfoRepository;
    @Autowired
    DownSpInfoRepository downSpInfoRepository;
    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void Upnotice(Map map) {

        logger.info("上游通知信息："+map);
        String outTradeNo = (String)map.get("out_trade_no");
        GatewaypayInfo gatewaypayInfo = gatewaypayInfoRepository.findByOutTradeNo(outTradeNo);

        if(null == gatewaypayInfoRepository.findNoticeStatus(outTradeNo)){
            ToDown(gatewaypayInfo);
        }


    }

    @Override
    public void ToDown(GatewaypayInfo gatewaypayInfo){
        logger.info("向下发送通知");
        Gson gson = new Gson();

        //拿到订单信息中的下游机构号，再拿密钥
        String down_sp_id = gatewaypayInfo.getDown_sp_id();
        Optional<DownSpInfo> downSpInfo = downSpInfoRepository.findById(down_sp_id);

        RSAPublicKey rsaPublicKey = null;
        RSAPrivateKey rsaPrivateKey = null;
        try {
            //获取公钥
            String down_pub_key = downSpInfo.get().getDown_pub_key();
            rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);
            //获取私钥
            String my_pri_key = downSpInfo.get().getMy_pri_key();
            rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        } catch (Exception e) {
            logger.error("向下通知信息获取密钥失败："+e);
        }

        Map map = new HashMap();

        if("SUCCESS".equals(gatewaypayInfo.getStatus())){
            map.put("status",gatewaypayInfo.getStatus());
            map.put("trade_state",gatewaypayInfo.getTrade_state());
            map.put("err_code",gatewaypayInfo.getErr_code());
            map.put("err_msg",gatewaypayInfo.getErr_msg());
        }else if("FAIL".equals(gatewaypayInfo.getStatus())){
            map.put("status",gatewaypayInfo.getStatus());
            map.put("code",gatewaypayInfo.getCode());
            map.put("message",gatewaypayInfo.getMessage());
        }

        //私钥签名
        String s = gson.toJson(map);
        map.put("sign", RSAUtils.sign(s,rsaPrivateKey));
        String responseInfoJson = gson.toJson(map);
        logger.info("向下通知信息："+responseInfoJson);

        // 通知结果
        String body = null;
        try {
            body = HttpUtil.post(gatewaypayInfo.getDown_notify_url(), responseInfoJson, 10000);
            if (null == body && !(body.equals("SUCCESS"))){
                logger.info("未收到响应，持续通知五次...");
                for (int count = 0;count < 5 ; count++){
                    HttpUtil.post(gatewaypayInfo.getDown_notify_url(), responseInfoJson, 10000);
                }
            }
        }catch (Exception e){
            logger.error("向下发送通知失败："+e);
        }
        String noticeStatus = "true";
        gatewaypayInfoRepository.updateNoticeStatus(noticeStatus,gatewaypayInfo.getOut_trade_no());
    }

}