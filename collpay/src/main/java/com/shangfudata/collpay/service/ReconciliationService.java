package com.shangfudata.collpay.service;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service
 */
public interface ReconciliationService {

    /**
     * 对账方法
     */
    void reconciliationInfo();

    /**
     * 上游为主对比系统
     */
    void upReconciliationSys();

    /**
     * 系统为主对比上游
     */
    void sysReconcilitionUp();

    /**
     * 下游下载系统对账文件
     */
    String downloadSysFile();
}
