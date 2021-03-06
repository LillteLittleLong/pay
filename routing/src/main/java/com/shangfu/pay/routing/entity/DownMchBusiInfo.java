package com.shangfu.pay.routing.entity;

import lombok.Data;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by tinlly to 2019/4/1
 * Package for com.shangfu.pay.service.entity
 */

@Data
@Entity
public class DownMchBusiInfo {

    /**
     * 下游商户通道业务信息
     */

    //下游商户业务表
    @Id
    @Column
    private Integer down_busi_id;
    //下游商户id
    @Column
    private String down_mch_id;
    //下游商户名称
    @Column
    private String down_mch_name;
    //下游商户可用业务
    @Column
    private String down_mch_busi_type;
    //最低手续费
    @Column
    private String min_charge;
    //抽成手续费
    @Column
    private String commis_charge;
    //开放时间
    @Column
    private String open_time;
    //关闭时间
    @Column
    private String close_time;
    //最小交易金额
    @Column
    private String min_amount;
    //最大交易限额
    @Column
    private String max_amount;
    //商户所属机构id
    @Column
    private String down_sp_id;

}