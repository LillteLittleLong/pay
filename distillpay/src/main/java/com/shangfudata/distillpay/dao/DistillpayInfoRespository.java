package com.shangfudata.distillpay.dao;


import com.shangfudata.distillpay.entity.DistillpayInfo;
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
public interface DistillpayInfoRespository extends JpaRepository<DistillpayInfo,String>, JpaSpecificationExecutor<DistillpayInfo>, Serializable {

    @Query("select d from DistillpayInfo d where d.trade_state =?1")
    List<DistillpayInfo> findByTradeState(String tradeState);

    @Query("select d from DistillpayInfo d where d.out_trade_no =?1")
    DistillpayInfo findByOutTradeNo(String OutTradeNo);


    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update DistillpayInfo e set e.trade_state =?1, e.err_code =?2, e.err_msg =?3 where e.out_trade_no =?4")
    void updateSuccessTradeState(String TradeState,String errCode,String errMsg,String outTradeNo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update DistillpayInfo e set e.status =?1, e.code =?2, e.message =?3 where e.out_trade_no =?4")
    void updateFailTradeState(String status,String code,String message,String out_trade_no);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update DistillpayInfo c set c.notice_status =?1 where c.out_trade_no =?2")
    void updateNoticeStatus(String noticeStatus,String OutTradeNo);

   /* @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update DistillpayInfo d set d.trade_state =?1, d.err_code =?2, d.err_msg =?3 where d.out_trade_no =?4")
    void updateByoutTradeNo(String tradeState, String errCode, String errMsg, String OutTradeNo);*/

    /*@Query("update DistillpayInfo c set c.notice_status =?1 where c.out_trade_no =?2")
    void updateNoticeStatus(String NoticeStatus,String OutTradeNo);*//*

    /*@Transactional
    @Modifying(clearAutomatically = true)
    @Query("update DistillpayInfo d set d.notice_status =?1 where d.out_trade_no =?2")
    void updateNoticeStatus(String noticeStatus, String OutTradeNo);*/

}
