package com.shangfudata.easypay.service.impl;

import cn.hutool.core.convert.Convert;
import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import com.shangfudata.easypay.dao.DistributionInfoRespository;
import com.shangfudata.easypay.dao.DownMchBusiInfoRespository;
import com.shangfudata.easypay.dao.EasypayInfoRespository;
import com.shangfudata.easypay.dao.UpMchBusiInfoRespository;
import com.shangfudata.easypay.entity.DistributionInfo;
import com.shangfudata.easypay.entity.DownMchBusiInfo;
import com.shangfudata.easypay.entity.EasypayInfo;
import com.shangfudata.easypay.entity.UpMchBusiInfo;
import com.shangfudata.easypay.service.NoticeService;
import com.shangfudata.easypay.service.SubmitService;
import com.shangfudata.easypay.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class SubmitServiceImpl implements SubmitService {
    


    @Autowired
    EasypayInfoRespository easypayInfoRespository;
    @Autowired
    UpMchBusiInfoRespository upMchBusiInfoRespository;
    @Autowired
    DownMchBusiInfoRespository downMchBusiInfoRespository;
    @Autowired
    DistributionInfoRespository distributionInfoRespository;

    String methodUrl = "http://192.168.88.65:8888/gate/epay/epsubmit";
    String signKey = "00000000000000000000000000000000";

    @Override
    public String submit(String sumbitInfoToJson) {
        Gson gson = new Gson();
        Map sumbitInfoToMap = gson.fromJson(sumbitInfoToJson, Map.class);
        String out_trade_no = (String)sumbitInfoToMap.get("out_trade_no");
        sumbitInfoToMap.replace("nonce_str", "123456789");
        sumbitInfoToMap.put("sign", SignUtils.sign(sumbitInfoToMap, signKey));
        String responseInfo = HttpUtil.post(methodUrl, sumbitInfoToMap, 12000);

        EasypayInfo response = gson.fromJson(responseInfo, EasypayInfo.class);

        String status = response.getStatus();
        //成功信息
        String trade_state = response.getTrade_state();
        String err_code = response.getErr_code();
        String err_msg = response.getErr_msg();
        //失败信息
        String code = response.getCode();
        String message = response.getMessage();


        if("SUCCESS".equals(status)){

            //清分
            EasypayInfo e = easypayInfoRespository.findByOutTradeNo(out_trade_no);
            Distribution(e);

            //将订单信息表存储数据库
            easypayInfoRespository.updateTradeState(trade_state,err_code,err_msg,out_trade_no);


        }else if("FAIL".equals(status)){
            easypayInfoRespository.updateFailTradeState(status,code,message,out_trade_no);
        }

        /*//向下通知
        noticeService.ToDown(easypayInfoRespository.findByOutTradeNo(out_trade_no));
        //noticeService.notice(collpayInfoRespository.findByOutTradeNo(collpayInfo.getOut_trade_no()));*/

        return responseInfo;
    }

    /**
     * 清分方法
     *
     * @param easypayInfo
     */
    public void Distribution(EasypayInfo easypayInfo){

        //计算清分,需要拿到{上游商户的手续费率，下游商户的手续费率}
        String down_mch_id = easypayInfo.getDown_mch_id();
        String mch_id = easypayInfo.getMch_id();

        //拿到上下游商户id后查询两张商户业务表
        DownMchBusiInfo downMchBusiInfo = downMchBusiInfoRespository.findByDownMchIdAndBusiType(down_mch_id, "easypay");
        //System.out.println("下游业务信息：："+downMchBusiInfo);
        UpMchBusiInfo upMchBusiInfo = upMchBusiInfoRespository.findByMchIdAndBusiType(mch_id, "easypay");
        //System.out.println("上游业务信息：："+upMchBusiInfo);

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
        if( (-1) == i){
            final_down_charge = down_min_charge;
        }else if( (1) == i ){
            final_down_charge = down_commis_charge.multiply(Total_fee);
        }else{
            final_down_charge = down_min_charge;
        }
        BigDecimal final_up_charge ;
        if( (-1) == j){
            final_up_charge = up_min_charge;
        }else if( (1) == j ){
            final_up_charge = up_commis_charge.multiply(Total_fee);
        }else{
            final_up_charge = up_min_charge;
        }

        //计算利润,先四舍五入小数位
        final_down_charge = final_down_charge.setScale(0, BigDecimal.ROUND_HALF_UP);
        final_up_charge = final_up_charge.setScale(0,BigDecimal.ROUND_HALF_UP);
        BigDecimal profit =final_down_charge.subtract(final_up_charge);

        DistributionInfo distributionInfo = new DistributionInfo();
        distributionInfo.setOut_trade_no(easypayInfo.getOut_trade_no());
        distributionInfo.setBusi_type("easypay");
        distributionInfo.setDown_mch_id(easypayInfo.getDown_mch_id());
        distributionInfo.setDown_charge(final_down_charge.toString());
        distributionInfo.setUp_mch_id(easypayInfo.getMch_id());
        distributionInfo.setUp_charge(final_up_charge.toString());
        distributionInfo.setProfit(profit.toString());
        distributionInfo.setTrad_amount(Total_fee.toString());

        //存数据库
        distributionInfoRespository.save(distributionInfo);
    }
}
