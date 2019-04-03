package com.shangfudata.collpay.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by tinlly to 2019/4/2
 * Package for com.shangfudata.collpay.entity
 */
@Data
@Entity
public class UpMchBusiInfo {

    @Id
    @Column
    private String up_busi_id;      // 上游商户业务id
    @Column
    private String mch_id;          // 上游商户id
    @Column
    private String mch_name;        // 上游商户名称
    @Column
    private String mch_busi_type;   // 商户可用业务
    @Column
    private String min_charge;      // 最低手续费
    @Column
    private String commis_charge;   // 抽成手续费
    @Column
    private String open_time;       // 开放时间
    @Column
    private String close_time;      // 关闭时间
    @Column
    private String min_amount;      // 最小交易金额
    @Column
    private String max_amount;      // 最大交易限额
    @Column
    private String sp_id;           // 商户所属通道id

}