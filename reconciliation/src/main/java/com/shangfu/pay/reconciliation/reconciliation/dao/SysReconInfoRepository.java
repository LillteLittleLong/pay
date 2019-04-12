package com.shangfu.pay.reconciliation.reconciliation.dao;

import com.shangfu.pay.reconciliation.reconciliation.entity.SysReconciliationInfo;
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
public interface SysReconInfoRepository extends JpaRepository<SysReconciliationInfo, String>, JpaSpecificationExecutor<SysReconciliationInfo> {

    @Query("from SysReconciliationInfo as s where s.trade_no = ?1")
    SysReconciliationInfo findByChTradeNo(String tradeNo);

    /**
     * @param tradeType
     * @return
     */
    @Query("from SysReconciliationInfo where trade_type = ?1")
    List<SysReconciliationInfo> queryUpReconciliationByTradeType(String tradeType);

    /**
     * 获取某个机构某天的对账信息
     * @param tradeTime
     * @return
     */
    @Query("from SysReconciliationInfo as s where s.trade_time like concat(?1 ,'%') and s.down_sp_id = ?2")
    List<SysReconciliationInfo> findByTradeTimeAndSpId(String tradeTime, String spId);

    /**
     * 根据 trade_no 改变 recon_state
     * @param recon_state
     * @param trade_no
     */
    @Query("update UpReconciliationInfo set recon_state = ?1 where trade_no = ?2")
    @Modifying
    @Transactional
    void updateReconStateByTradeNo(String recon_state, String trade_no);




}
