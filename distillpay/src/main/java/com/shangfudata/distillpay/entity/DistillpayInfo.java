package com.shangfudata.distillpay.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

@Entity
@Data
public class DistillpayInfo implements Serializable {


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
    @Column(length = 100,nullable = false)
    private String body;                    //描述
    @Column(nullable = false)
    private String total_fee;               //总金额
    @Column(nullable = false)
    private String settle_acc_type;         //账户类型
    @Column(nullable = false)
    private String bank_name;               //银行名称
    @Column(nullable = false)
    private String bank_no;                 //联行号
    @Column(nullable = false)
    private String card_name;               //账户名称
    @Column(nullable = false)
    private String card_no;                 //账户号
    @Column
    private String id_type;                 //证件类型，对私必填
    @Column
    private String id_no;                   //证件号码，对私必填


    /**
     * 每次请求或响应必带参数
     */
    @Column(length = 32)
    private String nonce_str;
    @Transient
    private String sign;


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

    /**
     * 通知信息
     */
    @Column
    private String notify_url;              //下游通知地址
    @Column
    private String notice_status;           //通知状态

}
