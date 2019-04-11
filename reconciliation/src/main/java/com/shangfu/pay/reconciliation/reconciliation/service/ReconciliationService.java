package com.shangfu.pay.reconciliation.reconciliation.service;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service
 */
public interface ReconciliationService {

    /**
     * 对账方法
     */
    void reconciliationInfo(String tradeType);

    /**
     * 上游为主对比系统
     */
    void upReconciliationSys(String tradeType);

    /**
     * 系统为主对比上游
     */
    void sysReconcilitionUp(String tradeType);

    /**
     * 下游下载系统对账文件
     */
    String downloadSysFile();
}
