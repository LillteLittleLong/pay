package com.shangfudata.easypay.dao;


import com.shangfudata.easypay.entity.EasypayInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

/**
 * 对传递的参数(交易信息)操作的持久层
 */

@Repository
public interface EasypayInfoRepository extends JpaRepository<EasypayInfo, String>, JpaSpecificationExecutor<EasypayInfo>, Serializable {

    @Query("select e from EasypayInfo e where e.trade_state =?1")
    List<EasypayInfo> findByTradeState(String tradeState);

    //@Query("select e from EasypayInfo e where e.settle_state =?1")
    //List<EasypayInfo> findBysettleState(String settleState);

    @Query("select e from EasypayInfo e where e.out_trade_no =?1")
    EasypayInfo findByOutTradeNo(String OutTradeNo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update EasypayInfo e set e.trade_state =?1, e.err_code =?2, e.err_msg =?3 where e.out_trade_no =?4")
    void updateTradeState(String TradeState, String errCode, String errMsg, String outTradeNo);

    //@Transactional
    //@Modifying(clearAutomatically = true)
    //@Query("update EasypayInfo e set e.trade_state =?1, e.err_code =?2, e.err_msg =?3, e.settle_state =?4 , e.settle_state_desc =?5 where e.out_trade_no =?6")
    //void updateSuccessTradeState(String TradeState, String errCode, String errMsg, String settleState, String settleStateDesc, String outTradeNo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update EasypayInfo e set e.status =?1, e.code =?2, e.message =?3 where e.out_trade_no =?4")
    void updateFailTradeState(String status, String code, String message, String out_trade_no);

    //@Transactional
    //@Modifying(clearAutomatically = true)
    //@Query("update EasypayInfo e set e.trade_state =?1 where e.out_trade_no =?2")
    //void updateTradeStateByOutTradeNo(String tradeState, String outTradeNo);

    @Query("select e.notice_status from EasypayInfo e where e.out_trade_no =?1")
    String findNoticeStatus(String outTradeNo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update EasypayInfo e set e.notice_status =?1 where e.out_trade_no =?2")
    void updateNoticeStatus(String noticeStatus, String outTradeNo);

}
