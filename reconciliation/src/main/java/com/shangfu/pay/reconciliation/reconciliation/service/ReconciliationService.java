package com.shangfu.pay.reconciliation.reconciliation.service;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service
 */
public interface ReconciliationService {

    /**
     * 上游为主对比系统
     */
    boolean upReconciliationSys(String tradeType);

    /**
     * 系统为主对比上游
     */
    boolean sysReconciliationUp(String tradeType);

}
