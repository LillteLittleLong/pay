package com.shangfudata.gatewaypay.dao;


import com.shangfudata.gatewaypay.entity.GatewaypayInfo;
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
public interface GatewaypayInfoRespository extends JpaRepository<GatewaypayInfo,String>, JpaSpecificationExecutor<GatewaypayInfo>, Serializable {

    @Query("select g from GatewaypayInfo g where g.trade_state =?1")
    List<GatewaypayInfo> findByTradeState(String tradeState);


    @Query("select g from GatewaypayInfo g where g.out_trade_no =?1")
    GatewaypayInfo findByOutTradeNo(String OutTradeNo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update GatewaypayInfo e set e.trade_state =?1, e.err_code =?2, e.err_msg =?3 where e.out_trade_no =?4")
    void updateSuccessTradeState(String TradeState,String errCode,String errMsg,String outTradeNo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update GatewaypayInfo e set e.status =?1, e.code =?2, e.message =?3 where e.out_trade_no =?4")
    void updateFailTradeState(String status,String code,String message,String out_trade_no);

    @Query("select g.notice_status from GatewaypayInfo g where g.out_trade_no =?1")
    String findNoticeStatus(String outTradeNo);
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update GatewaypayInfo g set g.trade_state =?1, g.err_code =?2, g.err_msg =?3, g.settle_state =?4, g.settle_state_desc =?5 ,g.ch_trade_no =?6 where g.out_trade_no =?7")
    void updateTradeState(String TradeState, String errCode, String errMsg ,String settleState,String settleStateDesc, String chTradeNo,String outTradeNo);


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update GatewaypayInfo g set g.notice_status =?1 where g.out_trade_no =?2")
    void updateNoticeStatus(String noticeStatus, String outTradeNo);


}
