package com.shangfudata.collpay;

import com.google.gson.Gson;
import com.shangfudata.collpay.controller.CollpayController;
import com.shangfudata.collpay.controller.QueryController;
import com.shangfudata.collpay.dao.CollpayInfoRespository;
import com.shangfudata.collpay.dao.DistributionInfoRespository;
import com.shangfudata.collpay.dao.DownSpInfoRespository;
import com.shangfudata.collpay.dao.SysReconInfoRepository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.DistributionInfo;
import com.shangfudata.collpay.entity.DownSpInfo;
import com.shangfudata.collpay.entity.SysReconciliationInfo;
import com.shangfudata.collpay.util.RSAUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CollpayApplicationTests {

    @Autowired
    CollpayController collpayController;
    @Autowired
    QueryController queryController;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;
    @Autowired
    CollpayInfoRespository collpayInfoRespository;
    @Autowired
    DistributionInfoRespository distributionInfoRespository;

    //@Test
    public void testCollpay() throws Exception {
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
        collpayInfo.setBody("废弃物发");
        collpayInfo.setTotal_fee("100999");
        collpayInfo.setCard_type("CREDIT");
        collpayInfo.setCard_name( "当前");
        collpayInfo.setCard_no("6217992900013005868");
        collpayInfo.setId_type("ID_CARD");
        collpayInfo.setId_no("342101196608282018");
        collpayInfo.setBank_mobile( "15563637881");
        collpayInfo.setCvv2("123");
        collpayInfo.setCard_valid_date("0318");
        //collpayInfo.setNotify_url("http://192.168.88.188:9001/consumer/notice");
        collpayInfo.setNotify_url("http://192.168.88.188:8101/notice");
        collpayInfo.setNonce_str("12345678901234567890123456789011");

        Gson gson = new Gson();
        String s = gson.toJson(collpayInfo);

        //私钥签名
        collpayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));
        System.out.println("未加密：："+collpayInfo);

        //公钥加密
        collpayInfo.setCard_name(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_name(), rsaPublicKey));
        collpayInfo.setCard_no(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_no(), rsaPublicKey));
        collpayInfo.setId_no(RSAUtils.publicKeyEncrypt(collpayInfo.getId_no(), rsaPublicKey));
        collpayInfo.setBank_mobile(RSAUtils.publicKeyEncrypt(collpayInfo.getBank_mobile(), rsaPublicKey));
        collpayInfo.setCvv2(RSAUtils.publicKeyEncrypt(collpayInfo.getCvv2(), rsaPublicKey));
        collpayInfo.setCard_valid_date(RSAUtils.publicKeyEncrypt(collpayInfo.getCard_valid_date(), rsaPublicKey));
        //String sign = collpayInfo.getSign();
        //System.out.println("签名信息"+sign);

        String collpayInfoToJson = gson.toJson(collpayInfo);
        System.out.println(collpayInfoToJson);
        //String collpay = collpayController.Collpay(collpayInfoToJson);
        //System.out.println(collpay);
    }

    @Test
    public void testQuery() throws Exception{
        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById("1001");

        //获取私钥
        String down_pri_key = downSpInfo.get().getDown_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(down_pri_key);

        CollpayInfo collpayInfo = new CollpayInfo();
        collpayInfo.setDown_sp_id("1001");
        collpayInfo.setOut_trade_no("1555469616188");
        collpayInfo.setNonce_str("12345678901234567890123456789011");

        Gson gson = new Gson();
        String s = gson.toJson(collpayInfo);

        collpayInfo.setSign(RSAUtils.sign(s,rsaPrivateKey));

        //String query = queryController.Query(s);
        System.out.println(gson.toJson(collpayInfo));
    }

    @Test
    public void saveCollPayRecon(){
        List<DistributionInfo> all1 = distributionInfoRespository.findAll();
        for (DistributionInfo collpayInfo : all1){
            CollpayInfo byOut_trade_no = collpayInfoRespository.findByOutTradeNo(collpayInfo.getOut_trade_no());
            if(null == byOut_trade_no){
                continue;
            }
            SysReconciliationInfo sysReconciliationInfo = new SysReconciliationInfo();
            sysReconciliationInfo.setSp_id("1000");
            sysReconciliationInfo.setTrade_time(byOut_trade_no.getTrade_time());
            //sysReconciliationInfo.setTrade_time("20190412105527");
            sysReconciliationInfo.setTrade_state(byOut_trade_no.getTrade_state());
            sysReconciliationInfo.setTotal_fee(byOut_trade_no.getTotal_fee());
            sysReconciliationInfo.setHand_fee(collpayInfo.getUp_charge());
            sysReconciliationInfo.setTrade_type("CP_PAY");
            sysReconciliationInfo.setSp_trade_no(byOut_trade_no.getOut_trade_no());
            sysReconciliationInfo.setTrade_no(byOut_trade_no.getCh_trade_no());
            sysReconciliationInfo.setDown_sp_id(byOut_trade_no.getDown_sp_id());
            sysReconciliationInfo.setDown_mch_id(byOut_trade_no.getDown_mch_id());

            sysReconInfoRepository.save(sysReconciliationInfo);
        }
    }

}