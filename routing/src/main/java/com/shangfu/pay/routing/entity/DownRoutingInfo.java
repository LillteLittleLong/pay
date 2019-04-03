package com.shangfu.pay.routing.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by tinlly to 2019/4/1
 * Package for com.shangfu.pay.service.one
 */
@Data
@Entity
public class DownRoutingInfo {

    // 路由 id
    @Id
    @Column
    private String routing_id;
    // 下游机构号
    @Column
    private String down_sp_id;
    // 下游商户号
    @Column
    private String down_mch_id;
    // 通道名称
    @Column
    private String passage;
    // 机构 id
    @Column
    private String mch_id;

}