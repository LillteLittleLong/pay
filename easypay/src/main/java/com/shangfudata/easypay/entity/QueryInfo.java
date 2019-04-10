package com.shangfudata.easypay.entity;

import lombok.Data;

import java.io.Serializable;


@Data
public class QueryInfo implements Serializable {

    /**
     * 查询请求信息
     */
    private String mch_id;                  //商户号
    private String out_trade_no;            //商户订单号
    private String nonce_str;               //随机字符串
    private String sign;                    //签名


    /**
     * 查询响应信息
     */
    private String status;                  //受理标识
    private String code;                    //错误代码
    private String message;                 //错误描述
    //private String out_trade_no;
    private String ch_trade_no;             //系统订单号
    private String trade_state;             //订单状态
    private String err_code;                //状态码
    private String err_msg;                 //状态说明
    private String settle_state;            //结算状态
    private String settle_state_desc;       //结算状态说明

}
