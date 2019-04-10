package com.shangfudata.gatewaypay.entity;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * Created by tinlly to 2019/3/26
 * Package for com.shangfu.pay.gateway.entity
 */
@Data
@Entity
public class GatewaypayInfo  {

    private static final long serialVersionUID = 2725982082086580313L;

    /**
     * 请求信息
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
    @Column(length = 32 , nullable = false)
    private String out_trade_no;            // 商户订单号
    @Column(length = 20 , nullable = false)
    private String total_fee;               // 总金额
    @Column(length = 100 , nullable = false)
    private String body;                    // 商品名称
    @Column(length = 100 , nullable = false)
    private String notify_url;              // 异步通知地址(接收上游)
    @Column(nullable = false)
    private String down_notify_url;         //下游的通知地址
    @Column(length = 100)
    private String call_back_url;           // 前台回显地址
    @Column(length = 32 , nullable = false)
    private String card_type;               // 卡类型
    @Column(length = 32 , nullable = false)
    private String bank_code;               // 支付银行

    /**
     * 每次请求或响应必带参数
     */
    @Column(length = 32)
    private String nonce_str;               // 随机字符串
    @Transient
    private String sign;                    // 签名

    /**
     * 响应结果
     */
    @Column(length = 20)
    private String status;                  // 受理表示
    @Column(length = 20)
    private String code;                    // 错误代码
    @Column(length = 200)
    private String message;                 // 错误描述


    @Column(length = 32)
    private String ch_trade_no;
    @Column(length = 32)
    private String trade_state;
    @Column(length = 32)
    private String err_code;
    @Column(length = 100)
    private String err_msg;
    @Column(length = 32)
    private String settle_state_desc;
    @Column(length = 32)
    private String settle_state;

    /**
     * 是否通知过的状态码
     */
    @Column(length = 32)
    private String notice_status;
}
