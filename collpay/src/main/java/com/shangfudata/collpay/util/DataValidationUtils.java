package com.shangfudata.collpay.util;

import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.exception.CollpayException;

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
    public DataValidationUtils cardTypeValid(String cardType, String cvv2, String cardValidData) throws CollpayException.CardTypeError, CollpayException.CreditParamIsNullException {
        switch (cardType) {
            case CollpayException.CardType.CREDIT:
                // 若为贷记卡
                if (cvv2.trim().equals("")) {
                    throw new CollpayException.CreditParamIsNullException();
                }
                if (cardValidData.trim().equals("")) {
                    throw new CollpayException.CreditParamIsNullException();
                }
                break;
            // 若为借记卡
            case CollpayException.CardType.DEBIT:
                break;
            default:
                throw new CollpayException.CardTypeError();
                // 卡类型错误
        }
        return this.dataValidationUtils;
    }

    /**
     * 证件类型效验
     */
    public DataValidationUtils cardValid(String idType, String idNo) throws CollpayException.IDTypeLengthException, CollpayException.IDTypeError {
        // id card 验证
        switch (idType) {
            case CollpayException.IDType.ID_CARD:
                // 证件验证
                if (!(RegexUtils.isIDCard18(idNo))) {
                    System.out.println("身份证验证错误");
                    // 不为银行卡号
                    throw new CollpayException.IDTypeLengthException();
                }
                break;
            default:
                // 证件类型错误
                throw new CollpayException.IDTypeError();
        }
        return this.dataValidationUtils;
    }

    /**
     * 持卡人姓名效验
     */
    public DataValidationUtils cardHolderNameValid(String cardHolderName) throws CollpayException.CardNameException {
        // 持卡人姓名长度小于 2 并且不为中文时抛出异常
        if (cardHolderName.length() < 2 || !RegexUtils.isZh(cardHolderName)) {
            // 持卡人姓名错误
            throw new CollpayException.CardNameException();
        }
        return this.dataValidationUtils;
    }

    /**
     * 校验银行卡卡号
     *
     * @return
     */
    public DataValidationUtils bankCardValid(String cardNo) throws CollpayException.BankCardIDException {
        if (!(RegexUtils.isBankCardNo(cardNo))) {
            throw new CollpayException.BankCardIDException();
        }
        return this.dataValidationUtils;
    }

    /**
     * 手机号效验
     *
     * @throws NullPointerException
     */
    public DataValidationUtils mobileNumberValid(String bankMobile) throws CollpayException.NotMobileNumberError {
        if (!(RegexUtils.isMobileExact(bankMobile))) {
            throw new CollpayException.NotMobileNumberError();
        }
        return this.dataValidationUtils;
    }

    /**
     * 随机字符串效验
     *
     * @throws NullPointerException
     */
    public DataValidationUtils nonceStrValid(String nonceStr) throws CollpayException.NonceStrLengthException {
        if (!(nonceStr.length() == 32)) {
            throw new CollpayException.NonceStrLengthException();
        }
        return this.dataValidationUtils;
    }


    /**
     * CollPay 下游请求参数异常处理方法
     */
    public void processCollPayException(CollpayInfo collpayInfo, Map responseMap) {
        // 数据效验
        // 异常处理
        try {
            dataValidationUtils.bankCardValid(collpayInfo.getCard_no()).cardValid(collpayInfo.getId_type(),
                    collpayInfo.getId_no()).cardTypeValid(collpayInfo.getCard_type(), collpayInfo.getCvv2(),
                    collpayInfo.getCard_valid_date()).cardHolderNameValid(collpayInfo.getCard_name()).
                    mobileNumberValid(collpayInfo.getBank_mobile()).nonceStrValid(collpayInfo.getNonce_str());
        } catch (CollpayException.NonceStrLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "随机字符串长度错误");
        } catch (CollpayException.NotMobileNumberError notMobileNumberError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "手机号码验证错误");
        } catch (CollpayException.CreditParamIsNullException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "贷记卡参数为空");
        } catch (CollpayException.CardTypeError cardTypeError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "银行卡类型错误");
        } catch (CollpayException.IDTypeLengthException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "证件号码错误");
        } catch (CollpayException.IDTypeError idTypeError) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "证件类型错误");
        } catch (CollpayException.BankCardIDException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "银行卡号错误");
        }catch (CollpayException.CardNameException e) {
            responseMap.put("status", "FAIL");
            responseMap.put("message", "持卡人姓名错误");
        }
    }
}
