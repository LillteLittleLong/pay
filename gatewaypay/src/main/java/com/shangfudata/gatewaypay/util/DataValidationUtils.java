package com.shangfudata.gatewaypay.util;


import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
import com.shangfudata.gatewaypay.exception.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by tinlly to 2019/3/28
 * Package for com.shangfudata.collpay.util
 */
public class DataValidationUtils {

    static DataValidationUtils dataValidationUtils;

    public static DataValidationUtils builder() {
        return dataValidationUtils = new DataValidationUtils();
    }

    Logger logger = LoggerFactory.getLogger(this.getClass());


    /**
     * 字段是否存在
     */
    public Map<String,String> isNullValid(Map<String, String> map,Map rsp) {

        //Map<String,String> rsp = new HashMap<>();

        if(map.containsKey("down_sp_id")){
            if(isEmpty(map.get("down_sp_id"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[sp_id]不能为空");
                logger.error("值为空:sp_id"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[sp_id]必填");
            logger.error("字段为空:sp_id"+rsp);
            return rsp;
        }
        if(map.containsKey("down_mch_id")){
            if(isEmpty(map.get("down_sp_id"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[sp_id]不能为空");
                logger.error("值为空:sp_id"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[mch_id]必填");
            logger.error("字段为空:mch_id"+rsp);
            return rsp;
        }
        if(map.containsKey("out_trade_no")){
            if(isEmpty(map.get("out_trade_no"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[out_trade_no]不能为空");
                logger.error("值为空:out_trade_no"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[out_trade_no]必填");
            logger.error("字段为空:out_trade_no"+rsp);
            return rsp;
        }
        if(map.containsKey("body")){
            if(isEmpty(map.get("body"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[body]不能为空");
                logger.error("值为空:body"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[body]必填");
            logger.error("字段为空:body"+rsp);
            return rsp;
        }
        if(map.containsKey("total_fee")){
            if(isEmpty(map.get("total_fee"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[total_fee]不能为空");
                logger.error("值为空:total_fee"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[total_fee]必填");
            logger.error("字段为空:total_fee"+rsp);
            return rsp;
        }
        if(map.containsKey("notify_url")){
            if(isEmpty(map.get("notify_url"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[notify_url]不能为空");
                logger.error("值为空:notify_url"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[notify_url]必填");
            logger.error("字段为空:notify_url"+rsp);
            return rsp;
        }
        if(map.containsKey("card_type")){
            if(isEmpty(map.get("card_type"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[card_type]不能为空");
                logger.error("值为空:card_type"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[card_type]必填");
            logger.error("字段为空:card_type"+rsp);
            return rsp;
        }
        if(map.containsKey("bank_code")){
            if(isEmpty(map.get("bank_code"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[bank_code]不能为空");
                logger.error("值为空:bank_code"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[bank_code]必填");
            logger.error("字段为空:bank_code"+rsp);
            return rsp;
        }

        if(map.containsKey("nonce_str")){
            if(isEmpty(map.get("nonce_str"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[nonce_str]不能为空");
                logger.error("值为空:nonce_str"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[nonce_str]必填");
            logger.error("字段为空:nonce_str"+rsp);
            return rsp;
        }
        if(map.containsKey("sign")){
            if(isEmpty(map.get("sign"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[sign]不能为空");
                logger.error("值为空:sign"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[sign]必填");
            logger.error("字段为空:sign"+rsp);
            return rsp;
        }
        return rsp;
    }
    /**
     * 参数的值是否为空判断
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            if ("".equals(obj.toString().trim())) {
                return true;
            }
        }
        if (obj instanceof List) {
            List list = (List) obj;
            if (list.size() == 0) {
                return true;
            }
        }
        if (obj instanceof Map) {
            Map map = (Map) obj;
            if (map.size() == 0) {
                return true;
            }
        }
        return false;
    }


    /**
     * 随机字符串效验
     *
     * @throws NullPointerException
     */
    public DataValidationUtils nonceStrValid(String nonceStr) throws MyException.NonceStrLengthException {
        if (!(nonceStr.length() == 32)) {
            throw new MyException.NonceStrLengthException();
        }
        return this.dataValidationUtils;
    }


    /**
     * Easypay 下游请求参数异常处理方法
     */
    public void processMyException(GatewaypayInfo gatewaypayInfo, Map responseMap) {
        // 数据效验
        // 异常处理
        try {
            dataValidationUtils.nonceStrValid(gatewaypayInfo.getNonce_str());
        } catch (MyException.NonceStrLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "随机字符串长度错误");
        }
    }
}
