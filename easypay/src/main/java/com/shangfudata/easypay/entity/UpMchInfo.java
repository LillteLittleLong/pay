package com.shangfudata.easypay.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by tinlly to 2019/4/4
 * Package for com.shangfudata.collpay.entity
 */
@Data
@Entity
public class UpMchInfo {

    @Id
    @Column
    private String mch_id;         // 上游商户id
    @Column
    private String mch_name;       // 上游商户名称
    @Column
    private String sec_key;        // aes加密密钥
    @Column
    private String sign_key;       // 签名密钥
    @Column
    private String sp_id;          // 所属通道id

}
