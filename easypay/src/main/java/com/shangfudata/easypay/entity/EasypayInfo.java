package com.shangfudata.easypay.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

@Entity
@Data
public class EasypayInfo implements Serializable {

    /**
     * 请求交易信息
     */

    @Column(length = 32,nullable = false)
    private String down_sp_id;              //下游机构服务商号
    @Column(length = 32,nullable = false)
    private String down_mch_id;             //下游机构商户号

    @Column(length = 32)
    private String sp_id;                   //上游机构服务商号
    @Column(length = 32)
    private String mch_id;                  //上游机构商户号


    @Id
    @Column(length = 32,nullable = false)
    private String out_trade_no;            //商户订单号
    @Column(length = 500,nullable = false)
    private String body;                    //描述
    @Column(length = 12,nullable = false)
    private String total_fee;               //总金额
    @Column(nullable = false)
    private String card_type;               //卡类型
    @Column(nullable = false)
    private String card_name;               //持卡人姓名
    @Column(nullable = false)
    private String card_no;                 //卡号
    @Column(nullable = false)
    private String id_type;                 //证件类型
    @Column(nullable = false)
    private String id_no;                   //证件号码
    @Column(nullable = false)
    private String bank_mobile;             //手机号
    @Column
    private String cvv2;                    //cvv2
    @Column
    private String card_valid_date;         //有效期
    @Column(nullable = false)
    private String notify_url;              //通知地址
    @Column(nullable = false)
    private String down_notify_url;         //下游的通知地址
    @Column(nullable = false)
    private String bank_code;               //银行编码
    @Column(nullable = false)
    private String bank_name;               //开户行名称

    @Transient
    private String password;                //验证码

    /**
     * 每次请求或响应必带参数
     */
    @Column(length = 32,nullable = false)
    private String nonce_str;               //随机字符串
    @Transient
    private String sign;                    //签名

    /**
     * 交易响应信息
     */

    @Column(length = 20)
    private String status;                  //受理标识
    @Column(length = 20)
    private String code;                    //错误代码
    @Column(length = 200)
    private String message;                 //错误描述

    @Column(length = 32)
    private String ch_trade_no;             //系统订单号
    @Column(length = 20)
    private String trade_state;             //订单状态
    @Column(length = 20)
    private String err_code;                //状态码
    @Column(length = 200)
    private String err_msg;                 //状态说明
    @Column(length = 32)
    private String settle_state_desc;       //结算状态
    @Column(length = 32)
    private String settle_state;            //结算状态说明


    /**
     * 是否通知过的状态码
     */
    @Column(length = 32)
    private String notice_status;

}
