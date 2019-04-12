package com.shangfu.pay.reconciliation.reconciliation.entity;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by tinlly to 2019/4/10
 * Package for com.shangfudata.collpay.entity
 */
@Data
@Entity
public class UpReconciliationInfo implements Serializable {

    /**
     * 上游机构对账表
     */

    //@Column
    //private String up_check_id;        // 上游对账 id

    @Id
    @Column
    private String trade_no;            // 系统订单号
    @Column
    private String sp_trade_no;         // 商户订单号
    @Column
    private String trade_time;          // 交易时间
    @Column
    private String sp_id;               // 上游机构号
    @Column
    private String trade_state;         // 交易状态
    @Column
    private String total_fee;           // 交易金额
    @Column
    private String hand_fee;            // 手续费
    @Column
    private String trade_type;          // 交易业务类型
    @Column
    private String recon_state;         // 对账状态

}