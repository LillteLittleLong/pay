package com.shangfudata.easypay.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.easypay.dao.*;
import com.shangfudata.easypay.entity.*;
import com.shangfudata.easypay.service.SubmitService;
import com.shangfudata.easypay.util.RSAUtils;
import com.shangfudata.easypay.util.SignUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SubmitServiceImpl implements SubmitService {

    @Autowired
    EasypayInfoRepository easypayInfoRepository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;
    @Autowired
    DistributionInfoRepository distributionInfoRepository;
    @Autowired
    DownSpInfoRepository downSpInfoRespository;
    @Autowired
    UpMchInfoRepository upMchInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    String methodUrl = "http://192.168.88.65:8888/gate/epay/epsubmit";

    @Override
    public String submit(String sumbitInfoToJson) {
        //创建一个map装返回信息
        Map<String,String> rsp = new HashMap();

        Gson gson = new Gson();

        Map jsonToMap = gson.fromJson(sumbitInfoToJson, Map.class);

        // 获取上游商户信息
        String sign = (String)jsonToMap.remove("sign");
        String down_sp_id = (String)jsonToMap.get("down_sp_id");
        String down_mch_id = (String)jsonToMap.get("down_mch_id");
        DownSpInfo downSpInfo = downSpInfoRespository.findBySpId(down_sp_id);

        //拿到密钥(公钥)
        String down_pub_key = downSpInfo.getDown_pub_key();
        RSAPublicKey rsaPublicKey = null;
        try {
            rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);
        } catch (Exception e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "密钥错误");
            logger.error("获取密钥错误:"+e);
            return gson.toJson(rsp);
        }
        //验签
        if (RSAUtils.doCheck(gson.toJson(jsonToMap), sign, rsaPublicKey)) {
            //随机字符串验证
            String nonce_str = (String)jsonToMap.get("nonce_str");
            if (!(nonce_str.length() == 32)) {
                rsp.put("status", "FAIL");
                rsp.put("message", "[nonce_str]随机字符串长度错误");
                logger.error("随机字符串长度错误");
                return gson.toJson(rsp);
            }
            EasypayInfo epayInfo = easypayInfoRepository.findByOutTradeNo((String)jsonToMap.get("out_trade_no"));
            logger.info("查询数据库订单信息："+epayInfo);

            jsonToMap.remove("down_sp_id");
            jsonToMap.remove("down_mch_id");
            // 更换签名、机构号、商户号
            jsonToMap.put("sp_id",epayInfo.getSp_id());
            jsonToMap.put("mch_id", epayInfo.getMch_id());
            UpMchInfo upMchInfo = upMchInfoRepository.findByMchId((String)jsonToMap.get("mch_id"));

            jsonToMap.put("sign", SignUtils.sign(jsonToMap, upMchInfo.getSign_key()));


            logger.info("向上信息"+jsonToMap);
            // 发送请求
            logger.info("向上请求提交订单...");
            String responseInfo = HttpUtil.post(methodUrl, jsonToMap, 12000);
            if (null == responseInfo) {
                logger.error("向上请求提交订单失败");
            }
            logger.info("向上请求提交订单成功：" + responseInfo);

            EasypayInfo response = gson.fromJson(responseInfo, EasypayInfo.class);


            epayInfo.setTrade_state(response.getTrade_state());
            epayInfo.setErr_code(response.getErr_code());
            epayInfo.setErr_msg(response.getErr_msg());
            epayInfo.setCode(response.getCode());
            epayInfo.setMessage(response.getMessage());

            // 设置交易时间
            epayInfo.setTrade_time(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
            if ("SUCCESS".equals(response.getStatus())) {
                //将订单信息表存储数据库

                EasypayInfo byOutTradeNo = easypayInfoRepository.save(epayInfo);

                //清分
                distribution(byOutTradeNo);
            } else if ("FAIL".equals(response.getStatus())) {
                easypayInfoRepository.save(epayInfo);
            }
            return responseInfo;
        }

        //验签失败，直接返回
        rsp.put("status", "FAIL");
        rsp.put("message", "[sign]签名错误**");
        logger.error("签名错误");
        return gson.toJson(rsp);



        // 获取 nonce_str sign_key
        //sumbitInfoToMap.replace("nonce_str", easypayInfo.getNonce_str());
        //sumbitInfoToMap.put("sign", SignUtils.sign(sumbitInfoToMap, upMchInfo.getSign_key()));

        //发送请求
        /*logger.info("向上请求提交订单...");
        String responseInfo = HttpUtil.post(methodUrl, sumbitInfoToMap, 12000);
        if (null == responseInfo) {
            logger.error("向上请求提交订单失败");
        }
        logger.info("向上请求提交订单成功：" + responseInfo);

        EasypayInfo response = gson.fromJson(responseInfo, EasypayInfo.class);

        String status = response.getStatus();

        EasypayInfo epayInfo = easypayInfoRepository.findByOutTradeNo(out_trade_no);
        epayInfo.setTrade_state(response.getTrade_state());
        epayInfo.setErr_code(response.getErr_code());
        epayInfo.setErr_msg(response.getErr_msg());
        epayInfo.setCode(response.getCode());
        epayInfo.setMessage(response.getMessage());

        // 设置交易时间
        epayInfo.setTrade_time(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
        if ("SUCCESS".equals(status)) {
            //将订单信息表存储数据库

            EasypayInfo byOutTradeNo = easypayInfoRepository.save(epayInfo);

            //清分
            distribution(byOutTradeNo);
        } else if ("FAIL".equals(status)) {
            easypayInfoRepository.save(epayInfo);
        }
        return responseInfo;*/
    }

    /**
     * 清分方法
     */
    public void distribution(EasypayInfo easypayInfo) {
        logger.info("清分计算信息：" + easypayInfo);
        //通过路由给的上下游两个商户业务 id 查询数据库 .
        DownMchBusiInfo downMchBusiInfo = downMchBusiInfoRepository.getOne(easypayInfo.getDown_busi_id());
        UpMchBusiInfo upMchBusiInfo = upMchBusiInfoRepository.getOne(easypayInfo.getUp_busi_id());

        //交易金额
        BigDecimal Total_fee = Convert.toBigDecimal(easypayInfo.getTotal_fee());
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
        distributionInfo.setOut_trade_no(easypayInfo.getOut_trade_no());
        distributionInfo.setBusi_type("easypay");
        distributionInfo.setDown_mch_id(easypayInfo.getDown_mch_id());
        distributionInfo.setDown_charge(final_down_charge.toString());
        distributionInfo.setUp_mch_id(easypayInfo.getMch_id());
        distributionInfo.setUp_charge(final_up_charge.toString());
        distributionInfo.setProfit(profit.toString());
        distributionInfo.setTrad_amount(Total_fee.toString());
        logger.info("清分结果：" + distributionInfo);

        SysReconciliationInfo sysReconciliationInfo = new SysReconciliationInfo();
        sysReconciliationInfo.setSp_id(easypayInfo.getSp_id());
        sysReconciliationInfo.setTrade_time(easypayInfo.getTrade_time());
        sysReconciliationInfo.setTrade_state(easypayInfo.getTrade_state());
        sysReconciliationInfo.setTotal_fee(easypayInfo.getTotal_fee());
        sysReconciliationInfo.setHand_fee(distributionInfo.getProfit());
        sysReconciliationInfo.setTrade_type("EPAY");
        sysReconciliationInfo.setSp_trade_no(easypayInfo.getOut_trade_no());
        sysReconciliationInfo.setTrade_no(easypayInfo.getCh_trade_no());
        sysReconciliationInfo.setDown_sp_id(easypayInfo.getDown_sp_id());
        sysReconciliationInfo.setDown_mch_id(easypayInfo.getDown_mch_id());

        // 保存系统对账信息
        sysReconInfoRepository.save(sysReconciliationInfo);
        //存数据库
        distributionInfoRepository.save(distributionInfo);
    }
}
