package com.shangfu.pay.routing.dao;

import com.shangfu.pay.routing.entity.DownMchBusiInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.io.Serializable;
import java.util.List;

/**
 * Created by tinlly to 2019/4/1
 * Package for com.shangfu.pay.service.dao
 */
public interface DownMchBusiInfoRepository extends JpaRepository<DownMchBusiInfo, Integer> , JpaSpecificationExecutor<DownMchBusiInfo> , Serializable {

    /**
     * 获取该商户所对应的通道
     * @return
     */
    @Query("from DownMchBusiInfo where down_sp_id = ?1 and down_mch_id = ?2 and down_mch_busi_type = ?3")
    DownMchBusiInfo queryMchPassage(String downSpId, String downMchId, String downMchBusiType);

}