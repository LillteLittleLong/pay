package com.shangfudata.gatewaypay.dao;


import com.shangfudata.gatewaypay.entity.DownMchBusiInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface DownMchBusiInfoRespository extends JpaRepository<DownMchBusiInfo,String>, JpaSpecificationExecutor<DownMchBusiInfo>, Serializable {


    @Query("select d from DownMchBusiInfo d where d.down_mch_id =?1 and d.down_mch_busi_type =?2")
    DownMchBusiInfo findByDownMchIdAndBusiType(String DownMchId, String BusiType);

}
