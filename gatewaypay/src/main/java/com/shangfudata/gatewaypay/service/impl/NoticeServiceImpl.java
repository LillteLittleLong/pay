package com.shangfudata.gatewaypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;

import com.shangfudata.gatewaypay.dao.DownSpInfoRespository;
import com.shangfudata.gatewaypay.dao.GatewaypayInfoRespository;
import com.shangfudata.gatewaypay.entity.DownSpInfo;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.service.NoticeService;
import com.shangfudata.gatewaypay.util.RSAUtils;
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
    GatewaypayInfoRespository gatewaypayInfoRespository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    JmsMessagingTemplate jmsMessagingTemplate;
    
    @Override
    public String Upnotice(Map<String,String> map) {

        Gson gson = new Gson();
        String s = gson.toJson(map);
        System.out.println("上游通知信息："+s);
        return "SUCCESS";
    }

    @Override
    public void ToDown(GatewaypayInfo gatewaypayInfo){
        Gson gson = new Gson();

        //拿到订单信息中的下游机构号，再拿密钥
        String down_sp_id = gatewaypayInfo.getDown_sp_id();
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

        // 通知计数
        int count = 0;
        // 通知结果
        String body = HttpUtil.post(gatewaypayInfo.getDown_notify_url(), responseInfoJson, 10000);

        while(!(body.equals("SUCCESS")) && count != 5){
            body = HttpUtil.post(gatewaypayInfo.getDown_notify_url(), responseInfoJson, 10000);
            count++;
        }
        String notice_status = "true";
        gatewaypayInfoRespository.updateNoticeStatus(notice_status,gatewaypayInfo.getOut_trade_no());

    }


}
