package com.shangfudata.easypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.easypay.dao.DownSpInfoRespository;
import com.shangfudata.easypay.dao.EasypayInfoRespository;
import com.shangfudata.easypay.entity.DownSpInfo;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.jms.EasypaySenderService;
import com.shangfudata.easypay.service.NoticeService;
import com.shangfudata.easypay.util.RSAUtils;
import org.apache.activemq.command.ActiveMQQueue;
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
    EasypayInfoRespository easypayInfoRespository;

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    
    @Override
    public String Upnotice(Map<String,String> map) {

        System.out.println("上游通知信息："+map);
        /*EasypayInfo byOutTradeNo = easypayInfoRespository.findByOutTradeNo(outTradeNo);
        if( ! "SUCCESS".equals(byOutTradeNo.getTrade_state())){
            easypayInfoRespository.updateTradeStateByOutTradeNo(tradeState,outTradeNo);
        }*/

        return "SUCCESS";
    }

    @Override
    public void ToDown(EasypayInfo easypayInfo) {

        Gson gson = new Gson();

        //拿到订单信息中的下游机构号，再拿密钥
        String down_sp_id = easypayInfo.getDown_sp_id();
        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);

        RSAPublicKey rsaPublicKey = null;
        RSAPrivateKey rsaPrivateKey = null;
        try{
            //获取公钥
            String down_pub_key = downSpInfo.get().getDown_pub_key();
            rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);
            //获取私钥
            String my_pri_key = downSpInfo.get().getMy_pri_key();
            rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        }catch(Exception e){

        }

        Map map = new HashMap();

        if("SUCCESS".equals(easypayInfo.getStatus())){
            map.put("status",easypayInfo.getStatus());
            map.put("trade_state",easypayInfo.getTrade_state());
            map.put("err_code",easypayInfo.getErr_code());
            map.put("err_msg",easypayInfo.getErr_msg());
        }else if("FAIL".equals(easypayInfo.getStatus())){
            map.put("status",easypayInfo.getStatus());
            map.put("code",easypayInfo.getCode());
            map.put("message",easypayInfo.getMessage());
        }

        //私钥签名
        String s = gson.toJson(map);
        map.put("sign", RSAUtils.sign(s,rsaPrivateKey));
        String responseInfoJson = gson.toJson(map);

        // 通知计数
        int count = 0;
        // 通知结果
        String body = HttpUtil.post(easypayInfo.getDown_notify_url(), responseInfoJson, 10000);

        while(!(body.equals("SUCCESS")) && count != 5){
            body = HttpUtil.post(easypayInfo.getDown_notify_url(), responseInfoJson, 10000);
            count++;
        }
        String notice_status = "true";
        easypayInfoRespository.updateNoticeStatus(notice_status,easypayInfo.getOut_trade_no());


    }


}
