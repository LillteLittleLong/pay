package com.shangfu.pay.reconciliation.reconciliation.dao;

import com.shangfu.pay.reconciliation.reconciliation.entity.ReconCheckInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * Created by tinlly to 2019/4/11
 * Package for com.shangfudata.collpay.dao
 */
public interface ReconCheckInfoRepository extends JpaRepository<ReconCheckInfo, String>, JpaSpecificationExecutor<ReconCheckInfo> {

    @Query("from ReconCheckInfo where trade_type = ?1")
    List<ReconCheckInfo> findByTrade_type(String tradeType);

    @Query("update ReconCheckInfo set up_check_status = ?1 where recon_check_id = ?2")
    @Modifying
    @Transactional
    void changeUpCheckStatusByReconCheckId(String upCheckStatus, String reconCheckId);

    @Query("update ReconCheckInfo set sys_check_status = ?1 where recon_check_id = ?2")
    @Modifying
    @Transactional
    void changeSysCheckStatusByReconCheckId(String sysCheckStatus, String reconCheckId);

    @Query("update ReconCheckInfo set sys_check_status = null , up_check_status = null")
    @Modifying
    @Transactional
    void updateReconCheck();

}
