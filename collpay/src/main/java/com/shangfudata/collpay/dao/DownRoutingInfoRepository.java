package com.shangfudata.collpay.dao;

import com.shangfudata.collpay.entity.DownRoutingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.io.Serializable;

/**
 * Created by tinlly to 2019/4/1
 * Package for com.shangfu.pay.service.dao
 */
public interface DownRoutingInfoRepository extends JpaRepository<DownRoutingInfo, Integer> , JpaSpecificationExecutor<DownRoutingInfo> , Serializable {

    @Query("from DownRoutingInfo where down_mch_id = ?1 and down_sp_id = ?2")
    DownRoutingInfo queryOne(String downMchId, String downSpId);

}