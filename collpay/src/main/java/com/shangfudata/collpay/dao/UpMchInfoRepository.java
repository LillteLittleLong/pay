package com.shangfudata.collpay.dao;

import com.shangfudata.collpay.entity.UpMchInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Created by tinlly to 2019/4/4
 * Package for com.shangfudata.collpay.dao
 */
public interface UpMchInfoRepository extends JpaRepository<UpMchInfo, String> , JpaSpecificationExecutor<UpMchInfo> {

}
