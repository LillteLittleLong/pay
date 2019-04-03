package com.shangfudata.distillpay.dao;

import com.shangfudata.distillpay.entity.UpMchBusiInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.io.Serializable;

@Repository
public interface UpMchBusiInfoRespository extends JpaRepository<UpMchBusiInfo,String>, JpaSpecificationExecutor<UpMchBusiInfo>, Serializable {

    @Query("select u from UpMchBusiInfo u where u.mch_id =?1 and u.mch_busi_type =?2")
    UpMchBusiInfo findByMchIdAndBusiType(String MchId, String BusiType);


}
