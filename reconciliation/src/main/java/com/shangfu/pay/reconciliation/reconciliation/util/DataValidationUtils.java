package com.shangfu.pay.reconciliation.reconciliation.util;

import com.shangfu.pay.reconciliation.reconciliation.entity.DownLoadInfo;
import com.shangfu.pay.reconciliation.reconciliation.exception.MyException;
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
        if (nonceStr.length()> 32) {
            throw new MyException.NonceStrLengthException();
        }
        return this.dataValidationUtils;
    }

    //流水日期校验
    public DataValidationUtils dataValid(String data) throws MyException.BillDateError {
        if (!(RegexUtils.isDate(data))) {
            throw new MyException.BillDateError();
        }
        return this.dataValidationUtils;
    }


    /**
     * CollPay 下游请求参数异常处理方法
     */
    public void processMyException(DownLoadInfo downLoadInfo, Map rsp) {
        // 数据效验
        // 异常处理
        try {
            dataValidationUtils.nonceStrValid(downLoadInfo.getNonce_str()).dataValid(downLoadInfo.getBill_date());
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
        }catch (MyException.BillDateError e) {
            rsp.put("status", "FAIL");
            rsp.put("message", "流水日期错误");
            logger.error("数据校验->流水日期错误"+e);
        }
    }
}
