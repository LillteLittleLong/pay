package com.shangfudata.easypay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.easypay.dao.EasypayInfoRepository;
import com.shangfudata.easypay.dao.UpMchInfoRepository;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.entity.QueryInfo;
import com.shangfudata.easypay.entity.UpMchInfo;
import com.shangfudata.easypay.service.NoticeService;
import com.shangfudata.easypay.service.QueryService;
import com.shangfudata.easypay.util.SignUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {

    String queryUrl = "http://192.168.88.65:8888/gate/spsvr/order/qry";

    @Autowired
    NoticeService noticeService;
    @Autowired
    EasypayInfoRepository easypayInfoRepository;
    @Autowired
    UpMchInfoRepository upMchInfoRepository;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 向上查询（轮询方法）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void queryToUp() {
        Gson gson = new Gson();

        //查询所有交易状态为NOTPAY的订单信息
        List<EasypayInfo> easypayInfoList = easypayInfoRepository.findByTradeState("NOTPAY");

        //遍历
        for (EasypayInfo easypayInfo : easypayInfoList) {
            //判断处理状态为SUCCESS的才进行下一步操作
            if ("SUCCESS".equals(easypayInfo.getStatus())) {
                //查询参数对象
                QueryInfo queryInfo = new QueryInfo();
                queryInfo.setMch_id(easypayInfo.getMch_id());
                queryInfo.setNonce_str(easypayInfo.getNonce_str());
                queryInfo.setOut_trade_no(easypayInfo.getOut_trade_no());

                //将queryInfo转为json，再转map
                String query = gson.toJson(queryInfo);
                Map queryMap = gson.fromJson(query, Map.class);

                // 获取上游商户信息
                UpMchInfo upMchInfo = upMchInfoRepository.findByMchId(easypayInfo.getMch_id());
                //签名
                queryMap.put("sign", SignUtils.sign(queryMap, upMchInfo.getSign_key()));
                logger.info("向上查询请求信息："+queryMap);

                //发送查询请求，得到响应信息
                String queryResponse = HttpUtil.post(queryUrl, queryMap, 6000);
                if(null == queryResponse){
                    logger.error("向上查询请求失败：");
                }else{
                    logger.info("向上查询请求响应信息："+queryResponse);
                }

                //使用一个新的UpeasypayInfo对象，接收响应参数
                EasypayInfo responseInfo = gson.fromJson(queryResponse, EasypayInfo.class);

                //如果交易状态发生改变，那就更新。
                if (!(responseInfo.getTrade_state().equals(easypayInfo.getTrade_state()))) {
                    //得到订单号
                    String out_trade_no = easypayInfo.getOut_trade_no();
                    String status = responseInfo.getStatus();
                    //成功信息
                    String trade_state = responseInfo.getTrade_state();
                    String err_code = responseInfo.getErr_code();
                    String err_msg = responseInfo.getErr_msg();
                    //失败信息
                    String code = responseInfo.getCode();
                    String message = responseInfo.getMessage();
                    if ("SUCCESS".equals(status)) {
                        //将订单信息表存储数据库
                        logger.info("交易成功："+queryResponse);
                        easypayInfoRepository.updateSuccessTradeState(trade_state, err_code, err_msg, out_trade_no);
                    } else if ("FAIL".equals(status)) {
                        logger.info("交易失败："+queryResponse);
                        easypayInfoRepository.updateFailTradeState(status, code, message, out_trade_no);
                    }

                }
            }
        }
    }


    /**
     * 下游查询方法
     *
     * @param easypayInfoToJson
     */
    //@Cacheable(value = "collpay", key = "#order.outTradeNo", unless = "#result.tradeState eq 'PROCESSING'")
    public String downQuery(String easypayInfoToJson) {
        //创建一个map装返回信息
        Map<String,String> rsp = new HashMap();
        Gson gson = new Gson();
        EasypayInfo easypayInfo = gson.fromJson(easypayInfoToJson, EasypayInfo.class);
        String out_trade_no = easypayInfo.getOut_trade_no();

        EasypayInfo finalEasypayInfo = easypayInfoRepository.findByOutTradeNo(out_trade_no);
        if(null ==finalEasypayInfo){
            rsp.put("status", "FAIL");
            rsp.put("message", "查询信息错误");
            logger.error("未查到订单，查询错误");
            return gson.toJson(rsp);
        }

        return gson.toJson(finalEasypayInfo);
    }
}
