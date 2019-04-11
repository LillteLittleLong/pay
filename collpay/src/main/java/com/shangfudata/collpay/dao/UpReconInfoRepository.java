package com.shangfudata.collpay.dao;

import com.shangfudata.collpay.entity.UpReconciliationInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by tinlly to 2019/4/10
 * Package for com.shangfudata.collpay.dao
 */
public interface UpReconInfoRepository extends JpaRepository<UpReconciliationInfo, String>, JpaSpecificationExecutor<UpReconciliationInfo> {

    /**
     *
     * @param tradeType
     * @return
     */
    @Query("from UpReconciliationInfo where trade_type = ?1")
    List<UpReconciliationInfo> queryUpReconciliationByTradeType(String tradeType);

    /**
     * 根据 trade_no 改变 recon_state
     * @param recon_state
     * @param trade_no
     */
    @Query("update UpReconciliationInfo set recon_state = ?1 where trade_no = ?2")
    @Modifying
    @Transactional
    void updateReconStateByTradeNo(String recon_state , String trade_no);

}
