package com.shangfudata.distillpay.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.distillpay.dao.*;
import com.shangfudata.distillpay.entity.*;
import com.shangfudata.distillpay.eureka.EurekaDistillpayClient;
import com.shangfudata.distillpay.jms.DistillpaySenderService;
import com.shangfudata.distillpay.service.DistillpayService;
import com.shangfudata.distillpay.util.AesUtils;
import com.shangfudata.distillpay.util.RSAUtils;
import com.shangfudata.distillpay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DistillpayServiceImpl implements DistillpayService {

    String methodUrl = "http://testapi.shangfudata.com/gate/rtp/distillpay";
    String signKey = "36D2F03FA9C94DCD9ADE335AC173CCC3";
    String aesKey = "45FBC053B1913EE83BE7C2801B263F3F";

    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    DistillpayInfoRespository distillpayInfoRespository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;
    @Autowired
    DistributionInfoRepository distributionInfoRepository;
    @Autowired
    DistillpaySenderService distillpaySenderService;
    @Autowired
    UpRoutingInfoRepository upRoutingInfoRepository;
    @Autowired
    EurekaDistillpayClient eurekaDistillpayClient;

    public String downDistillpay(String distillpayInfoToJson) throws Exception{
        //创建一个map装返回信息
        Map responseMap = new HashMap();
        //创建一个工具类对象
        //DataValidationUtils dataValidationUtils = DataValidationUtils.builder();

        Gson gson = new Gson();

        Map map = gson.fromJson(distillpayInfoToJson, Map.class);

        //验空
        //String message = dataValidationUtils.isNullValid(map);
        //if (!(message.equals(""))) {
        //    responseMap.put("status", "FAIL");
        //    responseMap.put("message", message);
        //    return gson.toJson(responseMap);
        //}

        //取签名
        String sign = (String)map.remove("sign");
        String s = gson.toJson(map);

        //下游传递上来的机构id，签名信息
        DistillpayInfo distillpayInfo = gson.fromJson(distillpayInfoToJson, DistillpayInfo.class);
        String down_sp_id = distillpayInfo.getDown_sp_id();

        Optional<DownSpInfo> downSpInfo = downSpInfoRespository.findById(down_sp_id);
        //拿到我自己（平台）的密钥(私钥)
        String my_pri_key = downSpInfo.get().getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        //拿到下游给的密钥(公钥)
        String down_pub_key = downSpInfo.get().getDown_pub_key();
        RSAPublicKey rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        //公钥验签
        if (RSAUtils.doCheck(s, sign, rsaPublicKey)){

            //私钥解密字段
            downDecoding(distillpayInfo, rsaPrivateKey);

            // 异常处理
            //dataValidationUtils.processMyException(distillpayInfo , responseMap);

            // 异常处理后判断是否需要返回
            if("FAIL".equals(responseMap.get("status"))){
                return gson.toJson(responseMap);
            }

            /* ------------------------ 路由分发 ------------------------------ */
            // 下游通道路由分发处理
            String downRoutingResponse = eurekaDistillpayClient.downRouting(distillpayInfo.getDown_mch_id(), distillpayInfo.getDown_sp_id(), distillpayInfo.getTotal_fee(), "distillpay");
            Map downRoutingMap = gson.fromJson(downRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(downRoutingMap.get("status"))) {
                return gson.toJson(downRoutingMap);
            }

            // 根据 down_sp_id 查询路由表 , 获取 mch_id sp_id
            UpRoutingInfo upRoutingInfo = upRoutingInfoRepository.queryByDownSpId(distillpayInfo.getDown_sp_id() , "distillpay");

            // 查看 上游通道路由分发处理
            String upRoutingResponse = eurekaDistillpayClient.upRouting(distillpayInfo.getDown_sp_id(), upRoutingInfo.getMch_id(), distillpayInfo.getTotal_fee(), "distillpay");
            Map upRoutingMap = gson.fromJson(upRoutingResponse, Map.class);

            // 无可用通道返回响应
            if ("FAIL".equals(upRoutingMap.get("status"))) {
                return gson.toJson(upRoutingMap);
            }
            /* ------------------------ 路由分发 ------------------------------ */

            // 无异常，保存下游请求信息到数据库
            distillpayInfoRespository.save(distillpayInfo);

            // 包装参数
            String DistillpayInfoToJson = gson.toJson(distillpayInfo);

            Map upDistillpayInfoMap = gson.fromJson(DistillpayInfoToJson, Map.class);
            upDistillpayInfoMap.put("down_busi_id", downRoutingMap.get("down_busi_id"));
            upDistillpayInfoMap.put("up_busi_id", upRoutingMap.get("up_busi_id"));
            upDistillpayInfoMap.put("mch_id" , upRoutingInfo.getMch_id());
            upDistillpayInfoMap.put("sp_id" , upRoutingInfo.getSp_id());
            String upDistillpayInfoJson = gson.toJson(upDistillpayInfoMap);

            // 将信息发送到队列中
            distillpaySenderService.sendMessage("distillpayinfoNotice.test",upDistillpayInfoJson);

            // 封装响应数据
            responseMap.put("sp_id",distillpayInfo.getDown_sp_id());
            responseMap.put("mch_id",distillpayInfo.getDown_mch_id());
            responseMap.put("status", "SUCCESS");
            responseMap.put("trade_state", "正在处理中");

            //返回响应参数
            return gson.toJson(responseMap);
        }

        //验签失败，直接返回
        responseMap.put("status", "FAIL");
        responseMap.put("message", "签名错误");
        return gson.toJson(responseMap);
    }

    /**
     * 向上交易方法
     * @param distillpayInfoToJson
     * @return
     * 1.设置上游机构号和商户号
     * 2.删除下游机构号和商户号以及签名
     * 3.向上签名，加密，发送请求
     * 4.收到响应信息，存入传上来的collpay对象
     * 5.判断，保存数据库
     */
    @JmsListener(destination = "distillpayinfoNotice.test")
    public void distillpayToUp(String distillpayInfoToJson){
        Gson gson = new Gson();
        System.out.println("上游接收请求消息 " + distillpayInfoToJson);

        Map distillpayInfoToMap = gson.fromJson(distillpayInfoToJson, Map.class);

        // 从 map 中删除并获取两个通道业务 id .
        String down_busi_id = (String) distillpayInfoToMap.remove("down_busi_id");
        String up_busi_id = (String) distillpayInfoToMap.remove("up_busi_id");

        //将json串转为对象，便于存储数据库
        String s = gson.toJson(distillpayInfoToMap);
        DistillpayInfo distillpayInfo = gson.fromJson(s,DistillpayInfo.class);

        //移除下游信息
        distillpayInfoToMap.remove("down_sp_id");
        distillpayInfoToMap.remove("down_mch_id");
        distillpayInfoToMap.remove("sign");
        distillpayInfoToMap.remove("notify_url");
        //对上交易信息进行签名
        distillpayInfoToMap.put("sign", SignUtils.sign(distillpayInfoToMap, signKey));

        //AES加密操作
        upEncoding(distillpayInfoToMap, aesKey);

        //发送请求
        String responseInfo = HttpUtil.post(methodUrl, distillpayInfoToMap, 12000);
        //获取响应信息，并用一个新DistillpayInfo对象装下这些响应信息
        DistillpayInfo response = gson.fromJson(responseInfo, DistillpayInfo.class);

        //将响应信息存储到当前downCollpayInfo及UpCollpayInfo请求交易完整信息中
        distillpayInfo.setTrade_state(response.getTrade_state());
        distillpayInfo.setStatus(response.getStatus());
        distillpayInfo.setCode(response.getCode());
        distillpayInfo.setMessage(response.getMessage());
        distillpayInfo.setCh_trade_no(response.getCh_trade_no());
        distillpayInfo.setErr_code(response.getErr_code());
        distillpayInfo.setErr_msg(response.getErr_msg());

        if("SUCCESS".equals(response.getStatus())){
            //将订单信息表存储数据库
            distillpayInfoRespository.save(distillpayInfo);

            // 清分参数包装
            Map<String, String> distributionMap = new HashMap<>();
            distributionMap.put("up_busi_id", up_busi_id);
            distributionMap.put("down_busi_id", down_busi_id);
            distributionMap.put("out_trade_no", distillpayInfo.getOut_trade_no());
            //清分
            distribution(distributionMap);
        }else if("FAIL".equals(response.getStatus())){
            distillpayInfoRespository.save(distillpayInfo);
        }
    }

    /**
     * RSA 解密方法
     *
     * @param distillpayInfo
     * @param rsaPrivateKey
     */
    public void downDecoding(DistillpayInfo distillpayInfo, RSAPrivateKey rsaPrivateKey) throws Exception {
        distillpayInfo.setCard_name(RSAUtils.privateKeyDecrypt(distillpayInfo.getCard_name(), rsaPrivateKey));
        distillpayInfo.setCard_no(RSAUtils.privateKeyDecrypt(distillpayInfo.getCard_no(), rsaPrivateKey));
        distillpayInfo.setId_no(RSAUtils.privateKeyDecrypt(distillpayInfo.getId_no(), rsaPrivateKey));
    }

    /**
     * AES 加密方法
     *
     * @param map
     * @param aesKey
     */
    public void upEncoding(Map map, String aesKey) {
        map.replace("card_name", AesUtils.aesEn((String) map.get("card_name"), aesKey));
        map.replace("card_no", AesUtils.aesEn((String) map.get("card_no"), aesKey));
        map.replace("id_no", AesUtils.aesEn((String) map.get("id_no"), aesKey));
    }

    /**
     * 清分方法
     *
     * @param collpayInfoMap
     */
    public void distribution(Map<String, String> collpayInfoMap) {
        //通过路由给的上下游两个商户业务 id 查询数据库 .
        DownMchBusiInfo downMchBusiInfo = downMchBusiInfoRepository.getOne(collpayInfoMap.get("down_busi_id"));
        UpMchBusiInfo upMchBusiInfo = upMchBusiInfoRepository.getOne(collpayInfoMap.get("up_busi_id"));

        // 根据订单号获取订单信息
        String out_trade_no = collpayInfoMap.get("out_trade_no");
        DistillpayInfo byOutTradeNo = distillpayInfoRespository.findByOutTradeNo(out_trade_no);

        //交易金额
        BigDecimal Total_fee = Convert.toBigDecimal(byOutTradeNo.getTotal_fee());
        //获取上下游商户最低手续费及相应手续费率
        BigDecimal down_commis_charge = Convert.toBigDecimal(downMchBusiInfo.getCommis_charge());
        BigDecimal down_min_charge = Convert.toBigDecimal(downMchBusiInfo.getMin_charge());
        BigDecimal up_commis_charge = Convert.toBigDecimal(upMchBusiInfo.getCommis_charge());
        BigDecimal up_min_charge = Convert.toBigDecimal(upMchBusiInfo.getMin_charge());

        //定义上下游最终手续费
        int i = down_commis_charge.multiply(Total_fee).compareTo(down_min_charge);
        int j = up_commis_charge.multiply(Total_fee).compareTo(up_min_charge);

        BigDecimal final_down_charge;
        if ((-1) == i) {
            final_down_charge = down_min_charge;
        } else if ((1) == i) {
            final_down_charge = down_commis_charge.multiply(Total_fee);
        } else {
            final_down_charge = down_min_charge;
        }
        BigDecimal final_up_charge;
        if ((-1) == j) {
            final_up_charge = up_min_charge;
        } else if ((1) == j) {
            final_up_charge = up_commis_charge.multiply(Total_fee);
        } else {
            final_up_charge = up_min_charge;
        }

        //计算利润,先四舍五入小数位
        final_down_charge = final_down_charge.setScale(0, BigDecimal.ROUND_HALF_UP);
        final_up_charge = final_up_charge.setScale(0, BigDecimal.ROUND_HALF_UP);
        BigDecimal profit = final_down_charge.subtract(final_up_charge);

        DistributionInfo distributionInfo = new DistributionInfo();
        distributionInfo.setOut_trade_no(byOutTradeNo.getOut_trade_no());
        distributionInfo.setBusi_type("collpay");
        distributionInfo.setDown_mch_id(byOutTradeNo.getDown_mch_id());
        distributionInfo.setDown_charge(final_down_charge.toString());
        distributionInfo.setUp_mch_id(byOutTradeNo.getMch_id());
        distributionInfo.setUp_charge(final_up_charge.toString());
        distributionInfo.setProfit(profit.toString());
        distributionInfo.setTrad_amount(Total_fee.toString());

        //存数据库
        distributionInfoRepository.save(distributionInfo);
    }

}
