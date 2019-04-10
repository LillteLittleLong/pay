package com.shangfudata.distillpay.util;



import com.shangfudata.distillpay.entity.DistillpayInfo;
import com.shangfudata.distillpay.exception.MyException;

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

    /**
     * 判断是否为空
     */
    public String isNullValid(Map<String, String> map) {
        for (Map.Entry<String, String> stringStringEntry : map.entrySet()) {
            try {
                isNullValid(stringStringEntry.getValue());
            } catch (NullPointerException e) {
                return stringStringEntry.getKey() + "不能为空";
            }
        }
        return "";
    }


    /**
     * 判断必填
     */
    public String isMust(Map<String, String> map) {
        if (map.containsKey("down_sp_id")&&map.containsKey("down_mch_id")&&map.containsKey("out_trade_no")&&map.containsKey("body")
                &&map.containsKey("total_fee")&&map.containsKey("settle_acc_type")&&map.containsKey("bank_name")&&map.containsKey("bank_no")
                &&map.containsKey("card_name")&&map.containsKey("card_no") &&map.containsKey("nonce_str"))
        {
            return "1";
        }
        else
        {
            return "必填字段缺失，请检查";
        }
    }

    /**
     * 判断必填
     */
    public String ismustquery(Map<String, String> map) {
        if (map.containsKey("down_mch_id")&&map.containsKey("out_trade_no")&&map.containsKey("nonce_str"))
        {
            return "1";
        }
        else
        {
            return "必填字段缺失，请检查";
        }
    }


    /**
     * 为空判断
     */
    public DataValidationUtils isNullValid(String string) throws NullPointerException {
        // string 判断为空
        if ("".equals(string) || null == string) {
            throw new NullPointerException();
        }
        return this.dataValidationUtils;
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
                    System.out.println("身份证验证错误");
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
     * Easypay 下游请求参数异常处理方法
     */
    public void processMyException(DistillpayInfo distillpayInfo, Map responseMap) {
        // 数据效验
        // 异常处理
        try {
            dataValidationUtils.bankCardValid(distillpayInfo.getCard_no()).cardValid(distillpayInfo.getId_type(),
                    distillpayInfo.getId_no()).cardHolderNameValid(distillpayInfo.getCard_name()).
                    nonceStrValid(distillpayInfo.getNonce_str());
        } catch (MyException.NonceStrLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "随机字符串长度错误");
        } catch (MyException.NotMobileNumberError notMobileNumberError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "手机号码验证错误");
        } catch (MyException.CreditParamIsNullException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "贷记卡参数为空");
        } catch (MyException.CardTypeError cardTypeError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "银行卡类型错误");
        } catch (MyException.IDTypeLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "证件号码错误");
        } catch (MyException.IDTypeError idTypeError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "证件类型错误");
        } catch (MyException.BankCardIDException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "银行卡号错误");
        }catch (MyException.CardNameException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "持卡人姓名错误");
        }
    }

    /**
     * EasyPay 下游请求参数异常处理方法
     */
    public void queryDistillpayException(DistillpayInfo easypayInfo, Map responseMap) {
        // 数据效验
        // 异常处理
        try {
            dataValidationUtils.nonceStrValid(easypayInfo.getNonce_str());
        } catch (MyException.NonceStrLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "随机字符串长度错误");
        }
    }
}
