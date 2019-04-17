package com.shangfudata.collpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.collpay.dao.CollpayInfoRespository;
import com.shangfudata.collpay.dao.DownSpInfoRespository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.DownSpInfo;
import com.shangfudata.collpay.service.NoticeService;
import com.shangfudata.collpay.util.RSAUtils;
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
    CollpayInfoRespository collpayInfoRespository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    Logger logger = LoggerFactory.getLogger(this.getClass());


    @Override
    public void notice(CollpayInfo collpayInfo) {

        Gson gson = new Gson();

        //拿到订单信息中的下游机构号，再拿密钥
        String down_sp_id = collpayInfo.getDown_sp_id();
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

        map.put("out_trade_no",collpayInfo.getOut_trade_no());
        map.put("total_fee",collpayInfo.getTotal_fee());

        if ("SUCCESS".equals(collpayInfo.getStatus())) {
            map.put("status", collpayInfo.getStatus());
            map.put("trade_state", collpayInfo.getTrade_state());
            map.put("err_code", collpayInfo.getErr_code());
            map.put("err_msg", collpayInfo.getErr_msg());

        } else if ("FAIL".equals(collpayInfo.getStatus())) {
            map.put("status", collpayInfo.getStatus());
            map.put("trade_state", collpayInfo.getTrade_state());
            map.put("code", collpayInfo.getCode());
            map.put("message", collpayInfo.getMessage());
        }

        //私钥签名
        String s = gson.toJson(map);
        map.put("sign", RSAUtils.sign(s, rsaPrivateKey));
        String responseInfoJson = gson.toJson(map);
        logger.info("向下通知信息："+responseInfoJson);


        // 通知结果
        String body = null;
        try {
            body = HttpUtil.post(collpayInfo.getNotify_url(), responseInfoJson, 10000);
            if (null == body && !(body.equals("SUCCESS"))){
                logger.info("未收到响应，持续通知五次...");
                for (int count = 0;count < 5 ; count++){
                    HttpUtil.post(collpayInfo.getNotify_url(), responseInfoJson, 10000);
                }
            }
        }catch (Exception e){
            logger.error("向下发送通知失败："+e);
        }

        /*try {
            if (null == body && !(body.equals("SUCCESS")));
            logger.info("未收到响应，持续通知五次...");
            for (int count = 0;count < 5 ; count++){
                body = HttpUtil.post(collpayInfo.getNotify_url(), responseInfoJson, 10000);
            }
        }catch(Exception e){
            logger.info("下游通知响应信息错误："+e);
        }*/
        String notice_status = "true";
        collpayInfoRespository.updateNoticeStatus(notice_status, collpayInfo.getOut_trade_no());
    }
}
