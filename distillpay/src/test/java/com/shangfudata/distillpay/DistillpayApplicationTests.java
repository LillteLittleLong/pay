package com.shangfudata.distillpay;

import com.google.gson.Gson;
import com.shangfudata.distillpay.controller.DistillpayController;
import com.shangfudata.distillpay.controller.QueryController;
import com.shangfudata.distillpay.dao.DownSpInfoRespository;
import com.shangfudata.distillpay.entity.DistillpayInfo;
import com.shangfudata.distillpay.entity.DownSpInfo;
import com.shangfudata.distillpay.util.RSAUtils;
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
public class DistillpayApplicationTests {

    @Autowired
    DownSpInfoRespository downSpInfoRespository;

    @Autowired
    DistillpayController distillpayController;

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

        DistillpayInfo distillpayInfo = new DistillpayInfo();
        distillpayInfo.setDown_sp_id("1001");
        distillpayInfo.setDown_mch_id("101");

        distillpayInfo.setOut_trade_no(System.currentTimeMillis() + "");
        distillpayInfo.setBody("符合无穷尽");
        distillpayInfo.setTotal_fee("110000");
        distillpayInfo.setSettle_acc_type("PERSONNEL");
        distillpayInfo.setBank_name("中国工商银行");
        distillpayInfo.setBank_no("102290032773");
        distillpayInfo.setCard_name("哈哈哈哈");
        distillpayInfo.setCard_no("6217992900013005868");
        distillpayInfo.setId_type("ID_CARD");
        distillpayInfo.setId_no("342101196608282018");
        distillpayInfo.setNonce_str("12345678901234567890123456789011");

        //公钥加密
        distillpayInfo.setCard_name(RSAUtils.publicKeyEncrypt(distillpayInfo.getCard_name(), rsaPublicKey));
        distillpayInfo.setCard_no(RSAUtils.publicKeyEncrypt(distillpayInfo.getCard_no(), rsaPublicKey));
        distillpayInfo.setId_no(RSAUtils.publicKeyEncrypt(distillpayInfo.getId_no(), rsaPublicKey));


        Gson gson = new Gson();
        String s = gson.toJson(distillpayInfo);

        //私钥签名
        distillpayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));
        /*String sign = easypayInfo.getSign();
        System.out.println("签名信息"+sign);*/

        String distillpayInfoToJson = gson.toJson(distillpayInfo);
        System.out.println(distillpayInfoToJson);

        //String distillpay = distillpayController.Distillpay(distillpayInfoToJson);
        //System.out.println(distillpay);
    }


    @Test
    public void testQuery(){
        DistillpayInfo distillpayInfo = new DistillpayInfo();
        distillpayInfo.setOut_trade_no("1553583029654");
        Gson gson = new Gson();
        String s = gson.toJson(distillpayInfo);
        System.out.println(s);
        //String query = queryController.Query(s);
        //System.out.println(query);
    }


}
