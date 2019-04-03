package com.shangfudata.distillpay.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * 清分表信息
 */

@Data
@Entity
public class DistributionInfo {

    @Id
    @Column
    private String out_trade_no;        // '商户订单号',
    @Column
    private String busi_type;           // '业务类型',
    @Column
    private String down_mch_id;         // '下游商户id',
    @Column
    private String down_charge;         // '下游手续费',
    @Column
    private String up_mch_id;           // '上游商户id',
    @Column
    private String up_charge;           // '上游手续费',
    @Column
    private String profit;              // '利润(下游手续费-上游手续费)',
    @Column
    private String trad_amount;         // '交易金额',


}
