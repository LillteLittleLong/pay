package com.shangfu.pay.routing.dao;

import com.shangfu.pay.routing.entity.UpMchBusiInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.io.Serializable;
import java.util.List;

/**
 * Created by tinlly to 2019/4/1
 * Package for com.shangfu.pay.service.dao
 */
public interface UpMchBusiInfoRepository extends JpaRepository<UpMchBusiInfo, Integer>, JpaSpecificationExecutor<UpMchBusiInfo>, Serializable {

    /**
     * 获取该商户所对应的通道
     * @return
     */
    @Query("from UpMchBusiInfo where passage_level = ?1 and mch_id = ?2 and mch_busi_type = ?3")
    List<UpMchBusiInfo> queryMchPassage(String passageLevel, String downMchId, String downMchBusiType);

    /**
     * 获取该商户所有的通道
     * @param downMchId
     * @param downMchBusiType
     * @return
     */
    @Query("from UpMchBusiInfo where down_mch_id = ?1 and mch_busi_type = ?2")
    List<UpMchBusiInfo> queryMchPassage(String downMchId, String downMchBusiType);

}