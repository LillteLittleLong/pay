package com.shangfudata.distillpay.dao;

import com.shangfudata.distillpay.entity.DownMchBusiInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import java.io.Serializable;
import java.util.List;

/**
 * Created by tinlly to 2019/4/1
 * Package for com.shangfu.pay.service.dao
 */
public interface DownMchBusiInfoRepository extends JpaRepository<DownMchBusiInfo, String> , JpaSpecificationExecutor<DownMchBusiInfo> , Serializable {

    /**
     * 获取该商户所对应的通道
     * @return
     */
    @Query("from DownMchBusiInfo where down_sp_id = ?1 and down_mch_id = ?2 and down_mch_busi_type = ?3")
    List<DownMchBusiInfo> queryMchPassage(String downSpId, String downMchId, String downMchBusiType);

    /**
     * 获取该商户所有的通道
     * @param downMchId
     * @param downMchBusiType
     * @return
     */
    @Query("from DownMchBusiInfo where down_mch_id = ?1 and down_mch_busi_type = ?2")
    List<DownMchBusiInfo> queryMchPassage(String downMchId, String downMchBusiType);

}