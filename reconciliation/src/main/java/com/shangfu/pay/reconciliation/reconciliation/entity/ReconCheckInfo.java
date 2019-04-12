package com.shangfu.pay.reconciliation.reconciliation.entity;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by tinlly to 2019/4/11
 * Package for com.shangfu.pay.reconciliation.reconciliation.entity
 */
@Data
@Entity
public class ReconCheckInfo {

    @Id
    @Column
    private String recon_check_id;          // 对账检查 id
    @Column
    private String sp_id;                   // 服务机构号
    @Column
    private String trade_type;              // 业务类型
    @Column
    private String up_check_status;         // 上游对账系统状态
    @Column
    private String sys_check_status;        // 系统对账下游状态

}