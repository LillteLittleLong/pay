package com.shangfudata.distillpay.dao;

import com.shangfudata.distillpay.entity.DistributionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface DistributionInfoRepository extends JpaRepository<DistributionInfo,String>, JpaSpecificationExecutor<DistributionInfo>, Serializable {

}
