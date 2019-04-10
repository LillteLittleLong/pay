package com.shangfudata.gatewaypay;

import com.google.gson.Gson;
import com.shangfudata.gatewaypay.controller.GatewaypayController;
import com.shangfudata.gatewaypay.controller.QueryController;
import com.shangfudata.gatewaypay.dao.DownSpInfoRespository;
import com.shangfudata.gatewaypay.entity.DownSpInfo;
import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.util.RSAUtils;
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
public class GatewaypayApplicationTests {

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    /*@Autowired
    GatewaypayController gatewaypayController;*/

    @Autowired
    QueryController queryController;

    @Test
    public void contextLoads() throws Exception{

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById("1001");

        //获取平台的公钥
        String my_pub_key = downSpInfo.get().getMy_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(my_pub_key);

        //获取下游自己的私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        GatewaypayInfo gatewaypayInfo = new GatewaypayInfo();
        gatewaypayInfo.setDown_sp_id("1001");
        gatewaypayInfo.setDown_mch_id("101");
        gatewaypayInfo.setOut_trade_no(System.currentTimeMillis() + "");
        gatewaypayInfo.setTotal_fee("8789");
        gatewaypayInfo.setBody("描述");
        gatewaypayInfo.setNotify_url("http://192.168.88.188:8104/gatewaypay/notice");
        gatewaypayInfo.setDown_notify_url("http://192.168.88.188:9001/consumer/notice");
        gatewaypayInfo.setCall_back_url("http://192.168.88.188:9001/consumer/success");
        gatewaypayInfo.setCard_type("CREDIT");
        gatewaypayInfo.setBank_code("01030000");
        gatewaypayInfo.setNonce_str("12345678901234567890123456789011");

        Gson gson = new Gson();
        String s = gson.toJson(gatewaypayInfo);
        //私钥签名
        gatewaypayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));
        String gatewaypayInfoToJson = gson.toJson(gatewaypayInfo);
        System.out.println(gatewaypayInfoToJson);
        //String gatewaypay = gatewaypayController.Gatewaypay(gatewaypayInfoToJson);
       // System.out.println(gatewaypay);
    }

    @Test
    public void testQuery(){
        GatewaypayInfo gatewaypayInfo = new GatewaypayInfo();
        gatewaypayInfo.setDown_mch_id("101");
        gatewaypayInfo.setOut_trade_no("1554359086875");
        gatewaypayInfo.setNonce_str("12345678901234567890123456789011");
        Gson gson = new Gson();
        String s = gson.toJson(gatewaypayInfo);
        //System.out.println(s);
       String query = queryController.Query(s);
        System.out.println(query);
    }

}
