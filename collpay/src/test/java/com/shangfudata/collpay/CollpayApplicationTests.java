package com.shangfudata.collpay;

import com.google.gson.Gson;
import com.shangfudata.collpay.controller.CollpayController;
import com.shangfudata.collpay.controller.QueryController;
import com.shangfudata.collpay.dao.DownSpInfoRespository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.DownSpInfo;
import com.shangfudata.collpay.util.RSAUtils;
import com.shangfudata.collpay.util.SignUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CollpayApplicationTests {

    @Autowired
    CollpayController collpayController;

    @Autowired
    QueryController queryController;

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    //@Test
    public void testCollpay() throws Exception{

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById("1001");

        //获取公钥
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //获取私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        Map<String, String> reqMap = new HashMap<String, String>();
        reqMap.put("down_sp_id", "1001");
        reqMap.put("down_mch_id", "101");
        reqMap.put("out_trade_no", System.currentTimeMillis() + "");
        reqMap.put("body", "test");
        reqMap.put("total_fee", "3000");
        reqMap.put("card_type", "CREDIT");
        reqMap.put("card_name", "小鱼仔");
        reqMap.put("card_no", "6222021001134258654");
        reqMap.put("id_type", "ID_CARD");
        reqMap.put("id_no", "410781199004016952");
        reqMap.put("bank_mobile", "12345678912");
        reqMap.put("cvv2", "123");
        reqMap.put("card_valid_date", "0318");
        //reqMap.put("notify_url", "http://192.168.168.168");
        reqMap.put("nonce_str", "123456789");
        //reqMap.put("sign", SignUtils.sign(reqMap, signKey));

        reqMap.put("card_name", RSAUtils.publicKeyEncrypt(reqMap.get("card_name"), rsaPublicKey));
        reqMap.put("card_no", RSAUtils.publicKeyEncrypt(reqMap.get("card_no"), rsaPublicKey));
        reqMap.put("id_no",RSAUtils.publicKeyEncrypt(reqMap.get("card_name"), rsaPublicKey));
        reqMap.put("bank_mobile",RSAUtils.publicKeyEncrypt(reqMap.get("card_name"), rsaPublicKey));
        reqMap.put("cvv2",RSAUtils.publicKeyEncrypt(reqMap.get("card_name"), rsaPublicKey));
        reqMap.put("card_valid_date",RSAUtils.publicKeyEncrypt(reqMap.get("card_name"), rsaPublicKey));

        Gson gson = new Gson();
        String s = gson.toJson(reqMap);

        reqMap.put("sign",RSAUtils.sign(s,rsaPrivateKey));
    }

    @Test
    public void contextLoads() throws Exception{
        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById("1001");

        //获取公钥
        String my_pub_key = downSpInfo.get().getMy_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(my_pub_key);

        //获取私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        CollpayInfo collpayInfo = new CollpayInfo();
        collpayInfo.setDown_sp_id("1001");
        collpayInfo.setDown_mch_id("101");

        collpayInfo.setOut_trade_no(System.currentTimeMillis() + "");
        collpayInfo.setBody("午饭晚饭呢");
        collpayInfo.setTotal_fee("100999");
        collpayInfo.setCard_type("CREDIT");
        collpayInfo.setCard_name( "嘿嘿嘿");
        collpayInfo.setCard_no("6217992900013005868");
        collpayInfo.setId_type("ID_CARD");
        collpayInfo.setId_no("342101196608282018");
        collpayInfo.setBank_mobile( "15563637881");
        collpayInfo.setCvv2("123");
        collpayInfo.setCard_valid_date("0318");
        collpayInfo.setNotify_url("http://192.168.88.188:9001/consumer/notice");
        collpayInfo.setNonce_str("12345678901234567890123456789011");

        //公钥加密
        collpayInfo.setCard_name(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_name(), rsaPublicKey));
        collpayInfo.setCard_no(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_no(), rsaPublicKey));
        collpayInfo.setId_no(RSAUtils.publicKeyEncrypt(collpayInfo.getId_no(), rsaPublicKey));
        collpayInfo.setBank_mobile(RSAUtils.publicKeyEncrypt(collpayInfo.getBank_mobile(), rsaPublicKey));
        collpayInfo.setCvv2(RSAUtils.publicKeyEncrypt(collpayInfo.getCvv2(), rsaPublicKey));
        collpayInfo.setCard_valid_date(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_valid_date(), rsaPublicKey));


        Gson gson = new Gson();
        String s = gson.toJson(collpayInfo);

        //私钥签名
        collpayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));
        //String sign = collpayInfo.getSign();
        //System.out.println("签名信息"+sign);

        String collpayInfoToJson = gson.toJson(collpayInfo);
        System.out.println("签名信息"+collpayInfoToJson);
        //String collpay = collpayController.Collpay(collpayInfoToJson);
        //System.out.println(collpay);
    }


    //@Test
    public void testQuery(){
        CollpayInfo collpayInfo = new CollpayInfo();
        collpayInfo.setOut_trade_no("1553148078245");
        Gson gson = new Gson();
        String s = gson.toJson(collpayInfo);
        String query = queryController.Query(s);
        System.out.println(query);
    }

}