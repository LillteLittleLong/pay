package com.shangfudata.easypay.exception;

public class MyException {

    public static class BankCardIDException extends RuntimeException{
        /**
        * 银行卡号错误
        */
    }
    public static class CardNameException extends RuntimeException {

        /**
         * 持卡人姓名错误
         */

    }


    public static class CardType {

        /**
         * 卡类型
         * DEBIT : 借记卡
         * CREDIT : 贷记卡
         */
        public static final String DEBIT = "DEBIT";
        public static final String CREDIT = "CREDIT";

    }
    public static class CardTypeError extends RuntimeException {

        /**
         * 银行卡类型错误
         */

    }
    public static class CreditParamIsNullException extends RuntimeException {

        /**
         * 贷记卡参数为空
         */

    }


    public static class IDType {

        /**
         * 证件类型
         * ID_CARD : 身份证
         */
        public static final String ID_CARD = "ID_CARD";

    }
    public static class IDTypeError extends RuntimeException {

        /**
         * 证件类型错误
         */

    }
    public static class IDTypeLengthException extends LengthError{

        /**
         * 证件号长度错误
         */

    }
    public static class LengthError extends RuntimeException {

        /**
         * 证件类型错误
         */

    }
    public static class NonceStrLengthException extends LengthError {

        /**
         * 证件类型长度错误
         */

    }
    public static class NotMobileNumberError extends RuntimeException {

        /**
         * 手机号码错误
         */

    }
}
