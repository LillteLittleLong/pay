package com.shangfudata.easypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.easypay.dao.DownSpInfoRepository;
import com.shangfudata.easypay.dao.EasypayInfoRepository;
import com.shangfudata.easypay.entity.DownSpInfo;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.jms.EasypaySenderService;
import com.shangfudata.easypay.service.NoticeService;
import com.shangfudata.easypay.util.RSAUtils;
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
    EasypayInfoRepository easypayInfoRepository;
    @Autowired
    DownSpInfoRepository downSpInfoRespository;
    @Autowired
    EasypaySenderService easypaySenderService;
    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Override
    public void Upnotice(Map map) {
        logger.info("上游通知信息："+map);
        String outTradeNo = (String)map.get("out_trade_no");
        EasypayInfo easypayInfo = easypayInfoRepository.findByOutTradeNo(outTradeNo);

        if(null == easypayInfoRepository.findNoticeStatus(outTradeNo)){
            ToDown(easypayInfo);
        }

    }

    @Override
    public void ToDown(EasypayInfo easypayInfo) {
        logger.info("向下发送通知");
        Gson gson = new Gson();

        //拿到订单信息中的下游机构号，再拿密钥
        String down_sp_id = easypayInfo.getDown_sp_id();
        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);

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

        map.put("out_trade_no",easypayInfo.getOut_trade_no());
        map.put("total_fee",easypayInfo.getTotal_fee());

        if ("SUCCESS".equals(easypayInfo.getStatus())) {
            map.put("status", easypayInfo.getStatus());
            map.put("trade_state", easypayInfo.getTrade_state());
            map.put("err_code", easypayInfo.getErr_code());
            map.put("err_msg", easypayInfo.getErr_msg());
        } else if ("FAIL".equals(easypayInfo.getStatus())) {
            map.put("status", easypayInfo.getStatus());
            map.put("trade_state", easypayInfo.getTrade_state());
            map.put("code", easypayInfo.getCode());
            map.put("message", easypayInfo.getMessage());
        }

        //私钥签名
        String s = gson.toJson(map);
        map.put("sign", RSAUtils.sign(s, rsaPrivateKey));
        String responseInfoJson = gson.toJson(map);
        logger.info("向下通知信息："+responseInfoJson);


        // 通知结果
        String body = null;
        try {
            body = HttpUtil.post(easypayInfo.getDown_notify_url(), responseInfoJson, 10000);
            if (null == body && !(body.equals("SUCCESS"))){
                logger.info("未收到响应，持续通知五次...");
                for (int count = 0;count < 5 ; count++){
                    HttpUtil.post(easypayInfo.getDown_notify_url(), responseInfoJson, 10000);
                }
            }
        }catch (Exception e){
            logger.error("向下发送通知失败："+e);
        }
        String noticeStatus = "true";
        easypayInfoRepository.updateNoticeStatus(noticeStatus,easypayInfo.getOut_trade_no());
    }

}
