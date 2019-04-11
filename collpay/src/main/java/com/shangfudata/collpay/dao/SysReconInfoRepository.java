package com.shangfudata.collpay.dao;

import com.shangfudata.collpay.entity.SysReconciliationInfo;
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
public interface SysReconInfoRepository extends JpaRepository<SysReconciliationInfo, String> , JpaSpecificationExecutor<SysReconciliationInfo> {

    @Query("from SysReconciliationInfo as s where s.trade_no = ?1")
    SysReconciliationInfo findByChTradeNo(String tradeNo);

    /**
     * 获取某个机构某天的对账信息
     * @param tradeTime
     * @return
     */
    @Query("from SysReconciliationInfo as s where s.trade_time like concat(?1 ,'%') and s.down_sp_id = ?2")
    List<SysReconciliationInfo> findByTradeTimeAndSpId(String tradeTime , String spId);


    /**
     * 根据 OutTradeNo 改变对账表交易状态
     * @param tradeState
     * @param OutTradeNo
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update CollpayInfo c set c.trade_state =?1 where c.out_trade_no =?2")
    void updateByOutTradeNo(String tradeState , String OutTradeNo);


}
