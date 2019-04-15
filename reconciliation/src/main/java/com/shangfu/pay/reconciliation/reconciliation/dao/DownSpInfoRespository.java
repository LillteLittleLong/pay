package com.shangfu.pay.reconciliation.reconciliation.dao;

import com.shangfu.pay.reconciliation.reconciliation.entity.DownSpInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.io.Serializable;

@Repository
public interface DownSpInfoRespository extends JpaRepository<DownSpInfo,String>, JpaSpecificationExecutor<DownSpInfo>, Serializable {

   /* @Query("select dmi from DownMchInfo dmi where dmi.mch_id = ?1")
    List<DownMchInfo> findByMchId(String down_mch_id);*/
   @Query("select dmi from DownSpInfo dmi where dmi.down_sp_id = ?1")
   DownSpInfo findBySpId(String down_mch_id);

}
