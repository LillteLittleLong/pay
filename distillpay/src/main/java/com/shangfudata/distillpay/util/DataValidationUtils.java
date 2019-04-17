package com.shangfudata.distillpay.util;

import com.shangfudata.distillpay.entity.DistillpayInfo;
import com.shangfudata.distillpay.exception.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by tinlly to 2019/3/28
 * Package for com.shangfudata.collpay.util
 */
public class DataValidationUtils {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    static DataValidationUtils dataValidationUtils;

    public static DataValidationUtils builder() {
        return dataValidationUtils = new DataValidationUtils();
    }

    /**
     * 字段是否存在
     */
    public Map<String,String> isNullValid(Map<String, String> map,Map rsp) {


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
        if(map.containsKey("settle_acc_type")){
            if(isEmpty(map.get("settle_acc_type"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[settle_acc_type]不能为空");
                logger.error("值为空:settle_acc_type"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[settle_acc_type]必填");
            logger.error("字段为空:settle_acc_type"+rsp);
            return rsp;
        }
        if(map.containsKey("card_name")){
            if(isEmpty(map.get("card_name"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[card_name]不能为空");
                logger.error("值为空:card_name"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[card_name]必填");
            logger.error("字段为空:card_name"+rsp);
            return rsp;
        }
        if(map.containsKey("card_no")){
            if(isEmpty(map.get("card_no"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[card_no]不能为空");
                logger.error("值为空:card_no"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[card_no]必填");
            logger.error("字段为空:card_no"+rsp);
            return rsp;
        }
        if(map.containsKey("bank_name")){
            if(isEmpty(map.get("bank_name"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[bank_name]不能为空");
                logger.error("值为空:bank_name"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[bank_name]必填");
            logger.error("字段为空:bank_name"+rsp);
            return rsp;
        }
        if(map.containsKey("bank_no")){
            if(isEmpty(map.get("bank_no"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[bank_no]不能为空");
                logger.error("值为空:bank_no"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[bank_no]必填");
            logger.error("字段为空:bank_no"+rsp);
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
     * 账户类型效验
     */
    public DataValidationUtils settleAccTypeValid(String settleAcctype, String idType, String idNo) throws MyException.settleAccTypeError{
        switch (settleAcctype) {
            // 若为对私
            case MyException.settleAccType.PERSONNEL:
                //验证证件类型
                cardValid(idType,idNo);
                break;
            // 若为对公
            case MyException.settleAccType.CORPORATE:
                break;
            default:
                throw new MyException.settleAccTypeError();
                // 卡类型错误
        }
        return this.dataValidationUtils;
    }

    /**
     * 证件类型效验
     */
    public DataValidationUtils cardValid(String idType, String idNo) throws MyException.IDTypeLengthException, MyException.IDTypeError {
        // id card 验证
        switch (idType) {
            case MyException.IDType.ID_CARD:
                // 证件验证
                if (!(com.shangfudata.distillpay.util.RegexUtils.isIDCard18(idNo))) {
                    // 不为身份证号
                    throw new MyException.IDTypeLengthException();
                }
                break;
            default:
                // 证件类型错误
                throw new MyException.IDTypeError();
        }
        return this.dataValidationUtils;
    }

    /**
     * 联行号效验
     */
    public DataValidationUtils bankNoValid(String bank_no) throws MyException.BankNoLengthException {
        // 持卡人姓名长度小于 2 并且不为中文时抛出异常
        if (bank_no.length() != 12) {
            // 持卡人姓名错误
            throw new MyException.BankNoLengthException();
        }
        return this.dataValidationUtils;
    }

    /**
     * 持卡人姓名效验
     */
    public DataValidationUtils cardHolderNameValid(String cardHolderName) throws MyException.CardNameException {
        // 持卡人姓名长度小于 2 并且不为中文时抛出异常
        if (cardHolderName.length() < 2 || !RegexUtils.isZh(cardHolderName)) {
            // 持卡人姓名错误
            throw new MyException.CardNameException();
        }
        return this.dataValidationUtils;
    }

    /**
     * 校验银行卡卡号
     *
     * @return
     */
    public DataValidationUtils bankCardValid(String cardNo) throws MyException.BankCardIDException {
        if (!(RegexUtils.isBankCardNo(cardNo))) {
            throw new MyException.BankCardIDException();
        }
        return this.dataValidationUtils;
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
    public void processMyException(Map<String,String> paramMap, Map responseMap) {
        // 数据效验
        // 异常处理
        try {
            dataValidationUtils.settleAccTypeValid(paramMap.get("settle_acc_type"),paramMap.get("id_type"),paramMap.get("id_no"))
            .bankNoValid(paramMap.get("bank_no")).cardHolderNameValid(paramMap.get("card_name")).bankCardValid(paramMap.get("card_no"))
            .nonceStrValid(paramMap.get("nonce_str"));
        } catch (MyException.settleAccTypeError settleAccTypeError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "账户类型错误");
            logger.error("数据校验->账户类型错误"+settleAccTypeError);
        } catch (MyException.IDTypeLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "证件号码错误");
            logger.error("数据校验->证件号码错误"+e);
        } catch (MyException.IDTypeError idTypeError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "证件类型错误");
            logger.error("数据校验->证件类型错误"+idTypeError);
        } catch (MyException.BankNoLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "联行号错误");
            logger.error("数据校验->联行号错误"+e);
        } catch (MyException.BankCardIDException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "银行卡号错误");
            logger.error("数据校验->银行卡号错误"+e);
        } catch (MyException.CardNameException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "持卡人姓名错误");
            logger.error("数据校验->持卡人姓名错误"+e);
        } catch (MyException.NonceStrLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "随机字符串长度为空");
            logger.error("数据校验->随机字符串错误"+e);
        }
    }
}
