package com.shangfu.pay.routing.entity;

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
public class UpRoutingInfo {

    @Id
    @Column
    private String up_routing_id;           // 下游机构号
    @Column
    private String mch_id;                  // 上游商户号
    @Column
    private String passage;                 // 通道
    @Column
    private String sp_id;                   // 下游商户 id

}
