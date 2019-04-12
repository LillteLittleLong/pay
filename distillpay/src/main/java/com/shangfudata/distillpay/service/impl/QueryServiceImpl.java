package com.shangfudata.distillpay.service.impl;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;

import com.shangfudata.distillpay.dao.DistillpayInfoRespository;
import com.shangfudata.distillpay.dao.SysReconInfoRepository;
import com.shangfudata.distillpay.dao.UpMchInfoRepository;
import com.shangfudata.distillpay.entity.DistillpayInfo;
import com.shangfudata.distillpay.entity.QueryInfo;
import com.shangfudata.distillpay.entity.UpMchInfo;
import com.shangfudata.distillpay.service.NoticeService;
import com.shangfudata.distillpay.service.QueryService;
import com.shangfudata.distillpay.util.SignUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class QueryServiceImpl implements QueryService {

    String methodUrl = "http://testapi.shangfudata.com/gate/spsvr/order/qry";

    @Autowired
    NoticeService noticeService;
    @Autowired
    DistillpayInfoRespository distillpayInfoRespository;
    @Autowired
    UpMchInfoRepository upMchInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 向上查询（轮询方法）
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void queryToUp() {
        Gson gson = new Gson();

        //查询所有交易状态为PROCESSING的订单信息
        List<DistillpayInfo> distillpayInfoList = distillpayInfoRespository.findByTradeState("PROCESSING");

        //遍历
        for (DistillpayInfo distillpayInfo : distillpayInfoList) {
            //判断处理状态为SUCCESS的才进行下一步操作
            if ("SUCCESS".equals(distillpayInfo.getStatus())) {
                //查询参数对象
                QueryInfo queryInfo = new QueryInfo();
                queryInfo.setMch_id(distillpayInfo.getMch_id());
                queryInfo.setNonce_str(distillpayInfo.getNonce_str());
                queryInfo.setOut_trade_no(distillpayInfo.getOut_trade_no());

                //获取上游商户信息
                UpMchInfo upMchInfo = upMchInfoRepository.queryByMchId(distillpayInfo.getMch_id());

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

                //使用一个新的UpdistillpayInfo对象，接收响应参数
                DistillpayInfo responseInfo = gson.fromJson(queryResponse, DistillpayInfo.class);
                //如果交易状态发生改变，那就更新。
                if (!(responseInfo.getTrade_state().equals(distillpayInfo.getTrade_state()))) {
                    //得到订单号
                    String out_trade_no = distillpayInfo.getOut_trade_no();
                    String status = responseInfo.getStatus();
                    //成功信息
                    String trade_state = responseInfo.getTrade_state();
                    String err_code = responseInfo.getErr_code();
                    String err_msg = responseInfo.getErr_msg();
                    //失败信息
                    String code = responseInfo.getCode();
                    String message = responseInfo.getMessage();

                    // 获取查询请求
                    //System.out.println("查询请求内容 > " + queryResponse);
                    if ("SUCCESS".equals(status)) {
                        logger.info("交易成功："+queryResponse);
                        //将订单信息表存储数据库
                        distillpayInfoRespository.updateSuccessTradeState(trade_state, err_code, err_msg, out_trade_no);
                    } else if ("FAIL".equals(status)) {
                        logger.info("交易失败："+queryResponse);
                        distillpayInfoRespository.updateFailTradeState(status, code, message, out_trade_no);
                    }
                    // 当上游订单交易状态发生改变的时候改变对账表对应的内容
                    sysReconInfoRepository.updateByOutTradeNo(trade_state , out_trade_no);
                    //发送通知
                    noticeService.notice(distillpayInfoRespository.findByOutTradeNo(out_trade_no));
                }
            }
        }
    }

    /**
     * 下游查询方法
     *
     * @param distillpayInfoToJson
     */
    public String downQuery(String distillpayInfoToJson) {
        Gson gson = new Gson();
        DistillpayInfo distillpayInfo = gson.fromJson(distillpayInfoToJson, DistillpayInfo.class);
        String out_trade_no = distillpayInfo.getOut_trade_no();

        DistillpayInfo finalDistillpayInfo = distillpayInfoRespository.findByOutTradeNo(out_trade_no);

        return gson.toJson(finalDistillpayInfo);
    }
}
