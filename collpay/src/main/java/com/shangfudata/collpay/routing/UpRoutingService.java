package com.shangfudata.collpay.routing;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import com.shangfudata.collpay.dao.UpMchBusiInfoRepository;
import com.shangfudata.collpay.dao.UpRoutingInfoRepository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.DownMchBusiInfo;
import com.shangfudata.collpay.entity.UpMchBusiInfo;
import com.shangfudata.collpay.entity.UpRoutingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tinlly to 2019/4/2
 * Package for com.shangfudata.collpay.service
 */
@Service
public class UpRoutingService {

    @Autowired
    UpRoutingInfoRepository upRoutingInfoRepository;
    @Autowired
    UpMchBusiInfoRepository upMchBusiInfoRepository;

    /**
     * 上游路由分发处理
     */

    /**
     * 获取数据查询数据库
     *
     * @param collpayInfo
     */
    public List<UpMchBusiInfo> upRouting(CollpayInfo collpayInfo) {
        String mch_id = collpayInfo.getMch_id();
        String sp_id = collpayInfo.getSp_id();

        UpRoutingInfo upRoutingInfo = upRoutingInfoRepository.queryOne(mch_id, sp_id);
        if (null == upRoutingInfo) {
            Console.error("上游没有可用通道 , 无法交易");
            //routingMap.put("status", "FAIL");
            //routingMap.put("message", "没有可用通道 , 无法交易");
            //return null;
        }

        // 获得了响应结果
        String spId = upRoutingInfo.getSp_id();
        String mchId = upRoutingInfo.getMch_id();
        String passage = upRoutingInfo.getPassage();
        String mchIdValid = upRoutingInfo.getMch_id();

        List<UpMchBusiInfo> upMchBusiInfo;

        // 获取该商户的所有通道
        if (mchIdValid.equals("*")) {
            System.out.println("走 * 号通道");
            upMchBusiInfo = upMchBusiInfoRepository.queryMchPassage(mchId, passage);
        } else {
            // 查询某个通道
            System.out.println("走单一通道");
            upMchBusiInfo = upMchBusiInfoRepository.queryMchPassage(spId, mchId, passage);
        }

        List<UpMchBusiInfo> upMchBusiInfos = passageValid(upMchBusiInfo, collpayInfo);
        if (0 == upMchBusiInfos.size()) {
            Console.error("上游没有可用通道 , 无法交易");
            //routingMap.put("status", "FAIL");
            //routingMap.put("message", "没有可用通道 , 无法交易");
            //return null;
        }

        return upMchBusiInfos;
    }

    /**
     * 可用通道验证
     *
     * @param upMchBusiInfo
     * @param collpayInfo
     */
    public List<UpMchBusiInfo> passageValid(List<UpMchBusiInfo> upMchBusiInfo, CollpayInfo collpayInfo) {
        // 符合通道的对象
        List<UpMchBusiInfo> mchBusiInfos = new ArrayList<>();

        for (UpMchBusiInfo mchBusiInfo : upMchBusiInfo) {
            // 获取当前时间
            DateTime date = DateUtil.date();
            int hour = DateUtil.hour(date, true);
            int minute = DateUtil.minute(date);
            System.out.println("24 小时格式当前时间 >> " + hour + "时 " + minute + "分");
            // 将时分合并成一个
            double currentTime = Double.parseDouble(hour + "." + minute);

            /* --------- 时间区间 --------- */
            double openTime = Double.parseDouble(mchBusiInfo.getOpen_time());
            double closeTime = Double.parseDouble(mchBusiInfo.getClose_time());

            if (openTime > currentTime || currentTime > closeTime) {
                Console.error("通道未到开启时间 , 暂时无法交易");
                continue;
            }

            /* --------- 金额区间 --------- */
            int minAmount = Integer.parseInt(mchBusiInfo.getMin_amount());
            int maxAmount = Integer.parseInt(mchBusiInfo.getMax_amount());
            // 订单价格
            int totalFee = Integer.parseInt(collpayInfo.getTotal_fee());

            if (minAmount > totalFee || totalFee > maxAmount) {
                Console.error("金额大小有误 , 无法交易");
                continue;
            }

            // 将符合的通道加入到集合中
            mchBusiInfos.add(mchBusiInfo);
        }
        return mchBusiInfos;
    }

}