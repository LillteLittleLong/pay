package com.shangfu.pay.reconciliation.reconciliation.entity;

import lombok.Data;

/**
 * Created by tinlly to 2019/4/10
 * Package for com.shangfu.pay.reconciliation.reconciliation.entity
 */
@Data
public class UpSpReconciliationInfo {

    private String sp_id;               // 服务商编号
    private String mcht_no;             // 商户编号
    private String trade_no;            // 系统订单号
    private String sp_trade_no;         // 服务商订单号
    private String trade_type;          // 订单类型
    private String total_fee;           // 订单金额
    private String trade_time;          // 订单时间
    private String trade_state;         // 订单状态
    private String hand_fee;            // 手续费
    private String settle_fee;          // 结算金额
    private String settle_rate_type;    // 收费类型
    private String settle_rate;         // 收费标准
    private String settle_min_fee;      // 最低收费
    private String extra_rate_type;     // 服务服收费类型
    private String extra_rate;          // 服务费收费标准
    private String extra_min_fee;       // 服务费最低收费

}
