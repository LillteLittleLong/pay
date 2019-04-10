package com.shangfudata.collpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.collpay.dao.CollpayInfoRespository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.QueryInfo;
import com.shangfudata.collpay.service.NoticeService;
import com.shangfudata.collpay.service.QueryService;
import com.shangfudata.collpay.util.DataValidationUtils;
import com.shangfudata.collpay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {


    String queryUrl = "http://testapi.shangfudata.com/gate/spsvr/order/qry";
    String signKey = "00000000000000000000000000000000";


    @Autowired
    CollpayInfoRespository collpayInfoRespository;
    @Autowired
    NoticeService noticeService;

    /**
     * 向上查询（轮询方法）
     */
    @Scheduled(cron = "*/60 * * * * ?")
    public void queryToUp() throws Exception {

        Gson gson = new Gson();

        //查询所有交易状态为PROCESSING的订单信息
        List<CollpayInfo> collpayInfoList = collpayInfoRespository.findByTradeState("PROCESSING");
        //遍历
        for (CollpayInfo collpayInfo : collpayInfoList) {
            //判断处理状态为SUCCESS的才进行下一步操作
            if ("SUCCESS".equals(collpayInfo.getStatus())) {
                //查询参数对象
                QueryInfo queryInfo = new QueryInfo();
                queryInfo.setMch_id(collpayInfo.getMch_id());
                queryInfo.setNonce_str(collpayInfo.getNonce_str());
                queryInfo.setOut_trade_no(collpayInfo.getOut_trade_no());
                //将queryInfo转为json，再转map
                String query = gson.toJson(queryInfo);
                Map queryMap = gson.fromJson(query, Map.class);
                queryMap.put("sign", SignUtils.sign(queryMap, signKey));
                //发送查询请求，得到响应信息
                String queryResponse = HttpUtil.post(queryUrl, queryMap, 6000);
                //使用一个新的UpCollpayInfo对象，接收响应参数
                CollpayInfo responseInfo = gson.fromJson(queryResponse, CollpayInfo.class);
                //如果交易状态发生改变，那就更新。
                if (!(responseInfo.getTrade_state().equals(collpayInfo.getTrade_state()))) {

                    //得到订单号
                    String out_trade_no = collpayInfo.getOut_trade_no();
                    String status = responseInfo.getStatus();
                    //得到交易状态信息
                    String trade_state = responseInfo.getTrade_state();
                    String err_code = responseInfo.getErr_code();
                    String err_msg = responseInfo.getErr_msg();
                    //失败信息
                    String code = responseInfo.getCode();
                    String message = responseInfo.getMessage();


                    if("SUCCESS".equals(status)){
                        //将订单信息表存储数据库
                        collpayInfoRespository.updateSuccessTradeState(trade_state,err_code,err_msg,out_trade_no);
                    }else if("FAIL".equals(status)){
                        collpayInfoRespository.updateFailTradeState(status,code,message,out_trade_no);
                    }
                    //发送通知
                    noticeService.notice(collpayInfoRespository.findByOutTradeNo(out_trade_no));
                }
            }
        }
    }


    /**
     * 下游查询方法
     *
     * @param collpayInfoToJson
     */
    @Cacheable(value = "collpay", key = "#order.outTradeNo", unless = "#result.tradeState eq 'PROCESSING'")
    public String downQuery(String collpayInfoToJson) {
        //创建一个map装返回信息
        Map responseMap = new HashMap();
        //创建一个工具类对象
        DataValidationUtils dataValidationUtils = DataValidationUtils.builder();

        Gson gson = new Gson();
        CollpayInfo collpayInfo = gson.fromJson(collpayInfoToJson, CollpayInfo.class);
        Map map = gson.fromJson(collpayInfoToJson, Map.class);


        //验必填
        String mess = dataValidationUtils.ismustquery(map);
        if (!(mess.equals("1"))) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", mess);
            return gson.toJson(responseMap);
        }

        //验空
        String message = dataValidationUtils.isNullValid(map);
        if (!(message.equals(""))) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", message);
            return gson.toJson(responseMap);
        }

        // 异常处理
        dataValidationUtils.queryCollpayException(collpayInfo , responseMap);
        // 异常处理后判断是否需要返回
        if("FAIL".equals(responseMap.get("status"))){
            return gson.toJson(responseMap);
        }

        String out_trade_no = collpayInfo.getOut_trade_no();
        CollpayInfo finalCollpayInfo = collpayInfoRespository.findByOutTradeNo(out_trade_no);
        return gson.toJson(finalCollpayInfo);
    }


}
