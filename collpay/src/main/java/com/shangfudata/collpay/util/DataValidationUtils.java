package com.shangfudata.collpay.util;

import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.exception.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;

/**
 * 数据校验
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
        if(map.containsKey("id_type")){
            if(isEmpty(map.get("id_type"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[id_type]不能为空");
                logger.error("值为空:id_type"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[id_type]必填");
            logger.error("字段为空:id_type"+rsp);
            return rsp;
        }
        if(map.containsKey("id_no")){
            if(isEmpty(map.get("id_no"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[id_no]不能为空");
                logger.error("值为空:id_no"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[id_no]必填");
            logger.error("字段为空:id_no"+rsp);
            return rsp;
        }
        if(map.containsKey("bank_mobile")){
            if(isEmpty(map.get("bank_mobile"))){
                rsp.put("status", "FAIL");
                rsp.put("message", "参数值[bank_mobile]不能为空");
                logger.error("值为空:bank_mobile"+rsp);
                return rsp;
            }
        }else{
            rsp.put("status", "FAIL");
            rsp.put("message", "参数[bank_mobile]必填");
            logger.error("字段为空:bank_mobile"+rsp);
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
     * 卡类型效验
     */
    public DataValidationUtils cardTypeValid(String cardType, String cvv2, String cardValidData) throws MyException.CardTypeError, MyException.CreditParamIsNullException {
        switch (cardType) {
            case MyException.CardType.CREDIT:
                // 若为贷记卡
                if (cvv2.trim().equals("")) {
                    throw new MyException.CreditParamIsNullException();
                }
                if (cardValidData.trim().equals("")) {
                    throw new MyException.CreditParamIsNullException();
                }
                break;
            // 若为借记卡
            case MyException.CardType.DEBIT:
                break;
            default:
                throw new MyException.CardTypeError();
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
                if (!(RegexUtils.isIDCard18(idNo))) {
                    //System.out.println("身份证验证错误");
                    // 不为银行卡号
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
     * 手机号效验
     *
     * @throws NullPointerException
     */
    public DataValidationUtils mobileNumberValid(String bankMobile) throws MyException.NotMobileNumberError {
        if (!(RegexUtils.isMobileExact(bankMobile))) {
            throw new MyException.NotMobileNumberError();
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
     * CollPay 下游请求参数异常处理方法
     */
    public void processMyException(CollpayInfo collpayInfo, Map rsp) {
        // 数据效验
        // 异常处理
        try {
            dataValidationUtils.bankCardValid(collpayInfo.getCard_no()).cardValid(collpayInfo.getId_type(),
                    collpayInfo.getId_no()).cardTypeValid(collpayInfo.getCard_type(), collpayInfo.getCvv2(),
                    collpayInfo.getCard_valid_date()).cardHolderNameValid(collpayInfo.getCard_name()).
                    mobileNumberValid(collpayInfo.getBank_mobile()).nonceStrValid(collpayInfo.getNonce_str());
        } catch (MyException.NonceStrLengthException e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "随机字符串长度错误");
            logger.error("数据校验->随机字符串错误"+e);
        } catch (MyException.NotMobileNumberError notMobileNumberError) {
            rsp.put("status", "FAIL");
            rsp.put("message", "手机号码验证错误");
            logger.error("数据校验->手机号码错误"+notMobileNumberError);
        } catch (MyException.CreditParamIsNullException e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "贷记卡参数为空");
            logger.error("数据校验->贷记卡参数错误"+e);
        } catch (MyException.CardTypeError cardTypeError) {
            rsp.put("status", "FAIL");
            rsp.put("message", "银行卡类型错误");
            logger.error("数据校验->银行卡类型错误"+cardTypeError);
        } catch (MyException.IDTypeLengthException e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "证件号码错误");
            logger.error("数据校验->证件号码错误"+e);
        } catch (MyException.IDTypeError idTypeError) {
            rsp.put("status", "FAIL");
            rsp.put("message", "证件类型错误");
            logger.error("数据校验->证件类型错误"+idTypeError);
        } catch (MyException.BankCardIDException e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "银行卡号错误");
            logger.error("数据校验->银行卡号错误"+e);
        }catch (MyException.CardNameException e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "持卡人姓名错误");
            logger.error("数据校验->持卡人姓名错误"+e);
        }
    }
}
