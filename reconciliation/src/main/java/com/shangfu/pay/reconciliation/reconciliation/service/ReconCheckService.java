package com.shangfu.pay.reconciliation.reconciliation.service;

/**
 * Created by tinlly to 2019/4/11
 * Package for com.shangfu.pay.reconciliation.reconciliation.service
 */
public interface ReconCheckService {

    void checkReconCollPay();

    void checkReconEasyPay();

    void checkReconGateWay();

    void checkReconDistillPay();

    boolean checkUpRecon(String tradeType);

    boolean checkSysRecon(String tradeType);

    void clearReconCheck();
}
