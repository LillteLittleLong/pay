package com.shangfudata.collpay.dao;

import com.shangfudata.collpay.entity.DistributionInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface DistributionInfoRespository extends JpaRepository<DistributionInfo,String>, JpaSpecificationExecutor<DistributionInfo>, Serializable {

}
