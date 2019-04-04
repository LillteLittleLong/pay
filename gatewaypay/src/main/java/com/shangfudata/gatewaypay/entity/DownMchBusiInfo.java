package com.shangfudata.gatewaypay.entity;

import lombok.Data;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 下游商户业务信息表：
 *
 */

@Data
@Entity
@Proxy(lazy = false)
public class DownMchBusiInfo implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String down_busi_id;            //下游业务id
    @Column
    private String down_mch_id;             //下游商户id
    @Column
    private String down_mch_name;           //下游商户名称
    @Column
    private String down_mch_busi_type;      //下游商户可用业务
    @Column
    private String min_charge;              //最低手续费
    @Column
    private String commis_charge;           //抽成手续费
    @Column
    private String open_time;               //开放时间
    @Column
    private String close_time;              //关闭时间
    @Column
    private String min_amount;              //最小交易金额
    @Column
    private String max_amount;              //最大交易限额
    @Column
    private String down_sp_id;              //商户所属机构id

}
