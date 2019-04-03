package com.shangfudata.collpay.dao;


import com.shangfudata.collpay.entity.CollpayInfo;
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
public interface CollpayInfoRespository extends JpaRepository<CollpayInfo,String>, JpaSpecificationExecutor<CollpayInfo>, Serializable {

    @Query("select c from CollpayInfo c where c.trade_state =?1")
    List<CollpayInfo> findByTradeState(String tradeState);

    @Query("select c from CollpayInfo c where c.out_trade_no =?1")
    CollpayInfo findByOutTradeNo(String OutTradeNo);

    /*@Query("update CollpayInfo c set c.notice_status =?1 where c.out_trade_no =?2")
    void updateNoticeStatus(String NoticeStatus,String OutTradeNo);*/

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update CollpayInfo e set e.trade_state =?1, e.err_code =?2, e.err_msg =?3 where e.out_trade_no =?4")
    void updateSuccessTradeState(String TradeState,String errCode,String errMsg,String outTradeNo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update CollpayInfo e set e.status =?1, e.code =?2, e.message =?3 where e.out_trade_no =?4")
    void updateFailTradeState(String status,String code,String message,String out_trade_no);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update CollpayInfo c set c.trade_state =?1, c.err_code =?2, c.err_msg =?3 where c.out_trade_no =?4")
    void updateByoutTradeNo(String tradeState, String errCode, String errMsg, String OutTradeNo);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update CollpayInfo c set c.notice_status =?1 where c.out_trade_no =?2")
    void updateNoticeStatus(String noticeStatus,String OutTradeNo);

}
