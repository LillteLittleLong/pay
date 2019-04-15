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
public class SysReconciliationInfo implements Serializable {

    /**
     * 系统对账表
     */

    //@Column
    //private String sys_check_id;        // 系统对账 id

    @Id
    @Column
    private String trade_no;            // 系统订单号
    @Column
    private String sp_trade_no;         // 商户订单号
    @Column
    private String trade_time;          // 交易时间
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
    @Column
    private String down_sp_id;          // 下游机构号
    @Column
    private String down_mch_id;         // 下游商户号
    //@Column
    //private String down_charge;         // 下游手续费
    @Column(length = 32)
    private String sp_id;                   //上游机构服务商号(系统对账失败时 , 用来获取上游对账信息)

    public void initNull(){
        this.trade_no = "";
        this.sp_trade_no = "";
        this.trade_time = "";
        this.trade_state = "";
        this.total_fee = "";
        this.hand_fee = "";
        this.trade_type = "";
        this.recon_state = "";
        this.down_sp_id = "";
        this.down_mch_id = "";
        //this.down_charge = "";
        this.sp_id = "";
    }

}
