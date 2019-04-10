package com.shangfudata.distillpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.distillpay.dao.DistillpayInfoRespository;
import com.shangfudata.distillpay.dao.DownSpInfoRespository;
import com.shangfudata.distillpay.entity.DistillpayInfo;
import com.shangfudata.distillpay.entity.DownSpInfo;
import com.shangfudata.distillpay.service.NoticeService;
import com.shangfudata.distillpay.util.RSAUtils;
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
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    DistillpayInfoRespository distillpayInfoRespository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void notice(DistillpayInfo distillpayInfo) {
        Gson gson = new Gson();

        //拿到订单信息中的下游机构号，再拿密钥
        String down_sp_id = distillpayInfo.getDown_sp_id();
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

        if("SUCCESS".equals(distillpayInfo.getStatus())){
            map.put("status",distillpayInfo.getStatus());
            map.put("trade_state",distillpayInfo.getTrade_state());
            map.put("err_code",distillpayInfo.getErr_code());
            map.put("err_msg",distillpayInfo.getErr_msg());
        }else if("FAIL".equals(distillpayInfo.getStatus())){
            map.put("status",distillpayInfo.getStatus());
            map.put("code",distillpayInfo.getCode());
            map.put("message",distillpayInfo.getMessage());
        }

        //私钥签名
        String s = gson.toJson(map);
        map.put("sign",RSAUtils.sign(s,rsaPrivateKey));
        String responseInfoJson = gson.toJson(map);
        logger.info("向下通知信息："+responseInfoJson);

        // 通知结果
        String body = null;
        try {
            body = HttpUtil.post(distillpayInfo.getNotify_url(), responseInfoJson, 10000);
            if (null == body && !(body.equals("SUCCESS"))){
                logger.info("未收到响应，持续通知五次...");
                for (int count = 0;count < 5 ; count++){
                    HttpUtil.post(distillpayInfo.getNotify_url(), responseInfoJson, 10000);
                }
            }
        }catch (Exception e){
            logger.error("向下发送通知失败："+e);
        }

        String notice_status = "true";
        distillpayInfoRespository.updateNoticeStatus(notice_status,distillpayInfo.getOut_trade_no());


    }
}