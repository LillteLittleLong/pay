package com.shangfu.pay.reconciliation.reconciliation.entity;

import lombok.Data;

@Data
public class DownLoadInfo {
    private String down_sp_id;               // 下游服务商编号
    private String bill_date;             // 流水日期
    private String nonce_str;            // 随机字符
    private String sign;         // 签名
}
