package com.shangfudata.collpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.collpay.dao.CollpayInfoRespository;
import com.shangfudata.collpay.dao.DownSpInfoRespository;
import com.shangfudata.collpay.dao.SysReconInfoRepository;
import com.shangfudata.collpay.dao.UpMchInfoRepository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.DownSpInfo;
import com.shangfudata.collpay.entity.QueryInfo;
import com.shangfudata.collpay.entity.UpMchInfo;
import com.shangfudata.collpay.exception.MyException;
import com.shangfudata.collpay.service.NoticeService;
import com.shangfudata.collpay.service.QueryService;
import com.shangfudata.collpay.util.DataValidationUtils;
import com.shangfudata.collpay.util.RSAUtils;
import com.shangfudata.collpay.util.SignUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {

    private String methodUrl = "http://testapi.shangfudata.com/gate/spsvr/order/qry";
    @Autowired
    CollpayInfoRespository collpayInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;
    @Autowired
    UpMchInfoRepository upMchInfoRepository;
    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    NoticeService noticeService;

    Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 向上查询（轮询方法）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void queryToUp() {
        Gson gson = new Gson();

        //查询所有交易状态为PROCESSING的订单信息
        List<CollpayInfo> collpayInfoList = collpayInfoRepository.findByTradeState("PROCESSING");

        //遍历
        for (CollpayInfo collpayInfo : collpayInfoList) {
            //判断处理状态为SUCCESS的才进行下一步操作
            if ("SUCCESS".equals(collpayInfo.getStatus())) {
                //查询参数对象
                QueryInfo queryInfo = new QueryInfo();
                queryInfo.setMch_id(collpayInfo.getMch_id());
                queryInfo.setNonce_str(collpayInfo.getNonce_str());
                queryInfo.setOut_trade_no(collpayInfo.getOut_trade_no());

                //获取上游商户信息
                UpMchInfo upMchInfo = upMchInfoRepository.queryByMchId(collpayInfo.getMch_id());

                //将queryInfo转为json，再转map
                String query = gson.toJson(queryInfo);
                Map queryMap = gson.fromJson(query, Map.class);
                //签名
                queryMap.put("sign", SignUtils.sign(queryMap, upMchInfo.getSign_key()));
                logger.info("向上查询请求信息："+queryMap);

                //发送查询请求，得到响应信息
                String queryResponse = HttpUtil.post(methodUrl, queryMap, 6000);
                if(null == queryResponse){
                    logger.error("向上查询请求失败：");
                }else{
                    logger.info("向上查询请求响应信息："+queryResponse);
                }

                //使用一个新的UpCollpayInfo对象，接收响应参数
                CollpayInfo responseInfo = gson.fromJson(queryResponse, CollpayInfo.class);

                //如果交易状态发生改变，那就更新。
                if (!(responseInfo.getTrade_state().equals(collpayInfo.getTrade_state()))) {
                    //得到订单号
                    String out_trade_no = collpayInfo.getOut_trade_no();
                    String status = responseInfo.getStatus();
                    //成功信息
                    String trade_state = responseInfo.getTrade_state();
                    String err_code = responseInfo.getErr_code();
                    String err_msg = responseInfo.getErr_msg();
                    //失败信息
                    String code = responseInfo.getCode();
                    String message = responseInfo.getMessage();


                    if ("SUCCESS".equals(status)) {
                        logger.info("交易成功："+queryResponse);
                        //将订单信息表存储数据库
                        collpayInfoRepository.updateSuccessTradeState(trade_state, err_code, err_msg, out_trade_no);
                    } else if ("FAIL".equals(status)) {
                        logger.info("交易失败："+queryResponse);
                        collpayInfoRepository.updateFailTradeState(status, code, message, out_trade_no);
                    }
                    // 当上游订单交易状态发生改变的时候改变对账表对应的内容
                    sysReconInfoRepository.updateByOutTradeNo(trade_state , out_trade_no);
                    //发送通知
                    noticeService.notice(collpayInfoRepository.findByOutTradeNo(out_trade_no));
                }
            }
        }
    }

    /**
     * 下游查询方法
     * @param collpayInfoToJson
     */
    public String downQuery(String collpayInfoToJson) {
        //创建一个map装返回信息
        Map<String,String> rsp = new HashMap();

        Gson gson = new Gson();

        Map<String,String> jsonToMap = gson.fromJson(collpayInfoToJson, Map.class);

        String sign = jsonToMap.remove("sign");
        String down_sp_id = jsonToMap.get("down_sp_id");
        DownSpInfo downSpInfo = downSpInfoRespository.findBySpId(down_sp_id);

        //拿到密钥(私钥)
        String my_pri_key = downSpInfo.getMy_pri_key();
        RSAPrivateKey rsaPrivateKey = null;
        //拿到密钥(公钥)
        String down_pub_key = downSpInfo.getDown_pub_key();
        RSAPublicKey rsaPublicKey = null;
        try {
            rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
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
            String nonce_str = jsonToMap.get("nonce_str");
            if (!(nonce_str.length() == 32)) {
                rsp.put("status", "FAIL");
                rsp.put("message", "[nonce_str]随机字符串长度错误");
                logger.error("随机字符串长度错误");
                return gson.toJson(rsp);
            }


            CollpayInfo finalCollpayInfo = collpayInfoRepository.findByOutTradeNo(jsonToMap.get("out_trade_no"));

            if(null == finalCollpayInfo){
                rsp.put("status", "FAIL");
                rsp.put("message", "查询信息错误");
                logger.error("未查到订单，查询错误");
                return gson.toJson(rsp);
            }
            rsp.put("status","SUCCESS");
            rsp.put("out_trade_no",finalCollpayInfo.getOut_trade_no());
            rsp.put("trade_state",finalCollpayInfo.getTrade_state());
            rsp.put("err_code",finalCollpayInfo.getErr_code());
            rsp.put("err_msg",finalCollpayInfo.getErr_msg());
            rsp.put("nonce_str", RandomStringUtils.randomAlphanumeric(10));
            rsp.put("sign",RSAUtils.sign(gson.toJson(rsp),rsaPrivateKey));
            return gson.toJson(rsp);
        }

        //验签失败，直接返回
        rsp.put("status", "FAIL");
        rsp.put("message", "[sign]签名错误");
        logger.error("签名错误");
        return gson.toJson(rsp);

    }


}


