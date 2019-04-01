package com.shangfudata.collpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.collpay.dao.CollpayInfoRespository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.QueryInfo;
import com.shangfudata.collpay.service.NoticeService;
import com.shangfudata.collpay.service.QueryService;
import com.shangfudata.collpay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
    public void queryToUp () throws Exception{

        Gson gson = new Gson();

        //查询所有交易状态为PROCESSING的订单信息
        List<CollpayInfo> collpayInfoList = collpayInfoRespository.findByTradeState("PROCESSING");

        //遍历
        for (CollpayInfo collpayInfo : collpayInfoList) {
            //System.out.println(collpayInfo);
            //判断处理状态为SUCCESS的才进行下一步操作
            if ("SUCCESS".equals(collpayInfo.getStatus())) {
                //if ("PROCESSING".equals(upCollpayInfo.getTrade_state())) {
                    //查询参数对象
                    QueryInfo queryInfo = new QueryInfo();
                    queryInfo.setMch_id(collpayInfo.getMch_id());
                    queryInfo.setNonce_str(collpayInfo.getNonce_str());
                    queryInfo.setOut_trade_no(collpayInfo.getOut_trade_no());

                    //将queryInfo转为json，再转map
                    String query = gson.toJson(queryInfo);
                    Map queryMap = gson.fromJson(query, Map.class);
                    queryMap.put("sign",SignUtils.sign(queryMap, signKey));

                    //发送查询请求，得到响应信息
                    String queryResponse = HttpUtil.post(queryUrl, queryMap, 6000);

                    //使用一个新的UpCollpayInfo对象，接收响应参数
                    CollpayInfo responseInfo = gson.fromJson(queryResponse, CollpayInfo.class);
                    //System.out.println(responseInfo);
                    // System.out.println(responseInfo);

                    //如果交易状态发生改变，那就更新。
                    if (!(responseInfo.getTrade_state().equals(collpayInfo.getTrade_state()))) {

                        //得到交易状态信息
                        String trade_state = responseInfo.getTrade_state();
                        //System.out.println("交易状态:"+trade_state);
                        String err_code = responseInfo.getErr_code();
                        //System.out.println("交易码:"+err_code);
                        String err_msg = responseInfo.getErr_msg();
                        //System.out.println("交易信息:"+err_msg);
                        //得到订单号
                        String out_trade_no = collpayInfo.getOut_trade_no();
                        //System.out.println("订单号"+out_trade_no);

                        //根据订单号，更新数据库交易信息表
                        collpayInfoRespository.updateByoutTradeNo(trade_state,err_code,err_msg,out_trade_no);
                        //发送通知
                        noticeService.notice(collpayInfoRespository.findByOutTradeNo(out_trade_no));
                    }
                //}
            }
        }
    }


    /**
     * 下游查询方法
     * @param collpayInfoToJson
     */
    @Cacheable(value = "collpay", key = "#order.outTradeNo", unless = "#result.tradeState eq 'PROCESSING'")
    public String downQuery(String collpayInfoToJson){
        Gson gson = new Gson();
        CollpayInfo collpayInfo = gson.fromJson(collpayInfoToJson, CollpayInfo.class);
        String out_trade_no = collpayInfo.getOut_trade_no();

        CollpayInfo finalCollpayInfo = collpayInfoRespository.findByOutTradeNo(out_trade_no);


        return gson.toJson(finalCollpayInfo);
    }



}
