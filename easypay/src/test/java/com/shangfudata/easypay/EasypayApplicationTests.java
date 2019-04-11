package com.shangfudata.easypay;

import com.google.gson.Gson;
import com.shangfudata.easypay.controller.EasypayController;
import com.shangfudata.easypay.controller.QueryController;
import com.shangfudata.easypay.controller.SubmitController;
import com.shangfudata.easypay.dao.DownSpInfoRepository;
import com.shangfudata.easypay.dao.EasypayInfoRepository;
import com.shangfudata.easypay.entity.DownSpInfo;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.util.RSAUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EasypayApplicationTests {

    @Autowired
    EasypayInfoRepository easypayInfoRespository;
    @Autowired
    DownSpInfoRepository downSpInfoRespository;
    @Autowired
    EasypayController easypayController;
    @Autowired
    SubmitController submitController;
    @Autowired
    QueryController queryController;

    @Test
    public void contextLoads() throws Exception{

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById("1001");

        //获取公钥
        String my_pub_key = downSpInfo.get().getMy_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(my_pub_key);

        //获取私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        EasypayInfo easypayInfo = new EasypayInfo();
        easypayInfo.setDown_sp_id("1001");
        easypayInfo.setDown_mch_id("101");

        easypayInfo.setOut_trade_no(System.currentTimeMillis() + "");
        easypayInfo.setBody("防火墙我");
        easypayInfo.setTotal_fee("88888");
        easypayInfo.setCard_type("CREDIT");
        easypayInfo.setCard_name( "应用");
        easypayInfo.setCard_no("6217992900013005868");
        easypayInfo.setId_type("ID_CARD");
        easypayInfo.setId_no("342101196608282018");
        easypayInfo.setBank_code("01030000");
        easypayInfo.setBank_name("农业银行");
        easypayInfo.setBank_mobile("15563637881");
        easypayInfo.setCvv2("123");
        easypayInfo.setCard_valid_date("0318");
        easypayInfo.setNotify_url("http://192.168.88.188:8102/easypay/notice");
        easypayInfo.setDown_notify_url("http://192.168.88.188:9001/consumer/notice");
        easypayInfo.setNonce_str("12345678901234567890123456789011");

        //公钥加密
        easypayInfo.setCard_name(RSAUtils.publicKeyEncrypt(easypayInfo.getCard_name(), rsaPublicKey));
        easypayInfo.setCard_no(RSAUtils.publicKeyEncrypt(easypayInfo.getCard_no(), rsaPublicKey));
        easypayInfo.setId_no(RSAUtils.publicKeyEncrypt(easypayInfo.getId_no(), rsaPublicKey));
        easypayInfo.setBank_mobile(RSAUtils.publicKeyEncrypt(easypayInfo.getBank_mobile(), rsaPublicKey));
        easypayInfo.setCvv2(RSAUtils.publicKeyEncrypt(easypayInfo.getCvv2(), rsaPublicKey));
        easypayInfo.setCard_valid_date(RSAUtils.publicKeyEncrypt(easypayInfo.getCard_valid_date(), rsaPublicKey));


        Gson gson = new Gson();
        String s = gson.toJson(easypayInfo);

        //私钥签名
        easypayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));
        /*String sign = easypayInfo.getSign();
        System.out.println("签名信息"+sign);*/

        String easypayInfoToJson = gson.toJson(easypayInfo);

        System.out.println(easypayInfoToJson);

        //String collpay = easypayController.Easypay(easypayInfoToJson);
        //System.out.println(collpay);

    }

    @Test
    public void testSubmit() {
        Gson gson = new Gson();

        EasypayInfo easypayInfo = new EasypayInfo();
        easypayInfo.setSp_id("1000");
        easypayInfo.setMch_id("100001000000000001");
        easypayInfo.setOut_trade_no("1554866408333");
        easypayInfo.setPassword("123456");
        easypayInfo.setNonce_str("12345678901234567890123456789011");


        String submitInfoToJson = gson.toJson(easypayInfo);
        System.out.println(submitInfoToJson);

        //submitController.Submit(submitInfoToJson);
    }

    @Test
    public void testQuery(){
        EasypayInfo collpayInfo = new EasypayInfo();
        collpayInfo.setOut_trade_no("1553677712904");
        Gson gson = new Gson();
        String s = gson.toJson(collpayInfo);
        System.out.println(s);
        //String query = queryController.Query(s);
        //System.out.println(query);
    }
}