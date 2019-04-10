package com.shangfudata.gatewaypay.dao;



import com.shangfudata.gatewaypay.entity.DownSpInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.io.Serializable;


@Repository
public interface DownSpInfoRespository extends JpaRepository<DownSpInfo,String>, JpaSpecificationExecutor<DownSpInfo>, Serializable {

   /* @Query("select dmi from DownMchInfo dmi where dmi.mch_id = ?1")
    List<DownMchInfo> findByMchId(String down_mch_id);*/

}
