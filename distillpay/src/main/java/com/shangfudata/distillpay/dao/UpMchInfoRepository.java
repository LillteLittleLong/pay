package com.shangfudata.distillpay.dao;

import com.shangfudata.distillpay.entity.UpMchInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * Created by tinlly to 2019/4/4
 * Package for com.shangfudata.collpay.dao
 */
public interface UpMchInfoRepository extends JpaRepository<UpMchInfo, String>, JpaSpecificationExecutor<UpMchInfo> {

    @Query("from UpMchInfo where mch_id = ?1")
    UpMchInfo queryByMchId(String mchId);
}
