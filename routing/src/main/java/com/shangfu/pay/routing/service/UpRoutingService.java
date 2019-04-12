package com.shangfu.pay.routing.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import com.shangfu.pay.routing.dao.UpMchBusiInfoRepository;
import com.shangfu.pay.routing.dao.UpRoutingInfoRepository;
import com.shangfu.pay.routing.entity.DownMchBusiInfo;
import com.shangfu.pay.routing.entity.UpMchBusiInfo;
import com.shangfu.pay.routing.entity.UpRoutingInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

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
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 上游路由分发处理
     */

    /**
     * 获取数据查询数据库
     *
     * @param map
     * @return 返回可用通道 id
     */
    public Integer upRouting(Map<String, String> map, Map routingMap) {
        //System.out.println("请求参数 >> " + ObjectUtil.toString(map));
        logger.info("请求参数 >> "+map);

        String down_sp_id = map.get("down_sp_id");
        String mch_id = map.get("mch_id");
        String passage = map.get("passage");
        UpRoutingInfo upRoutingInfo = upRoutingInfoRepository.queryOne(down_sp_id, mch_id , passage);
        if (null == upRoutingInfo) {
            logger.error("没有可用通道 , 无法交易 >> "+map);
            routingMap.put("status", "FAIL");
            routingMap.put("message", "没有可用通道 , 无法交易");
            return -1;
        }

        // 获得了响应结果
        //String downSpId = upRoutingInfo.getDown_sp_id();
        //String mchId = upRoutingInfo.getMch_id();
        //String passage = upRoutingInfo.getPassage();
        String spId = upRoutingInfo.getSp_id();

        List<UpMchBusiInfo> upMchBusiInfo;

        // 获取该商户的所有通道
        if (spId.equals("*")) {
            logger.info("上游走 * 号通道");
            upMchBusiInfo = upMchBusiInfoRepository.queryMchPassage(mch_id, passage);
        } else {
            // 查询某个通道
            logger.info("上游走单一通道");
            upMchBusiInfo = upMchBusiInfoRepository.queryMchPassage(spId, mch_id, passage);
        }

        List<UpMchBusiInfo> upMchBusiInfos = passageValid(upMchBusiInfo, map);

        if (0 == upMchBusiInfos.size()) {
            logger.error("上游没有可用通道 , 无法交易");
            routingMap.put("status", "FAIL");
            routingMap.put("message", "没有可用通道 , 无法交易");
            return -1;
        } else if (1 < upMchBusiInfos.size()) { // 如果有多个通道 , 返回利润最高的通道
            return passageChoose(upMchBusiInfos, map);
        }

        return upMchBusiInfos.get(0).getUp_busi_id();
    }

    /**
     * 可用通道验证
     *
     * @param upMchBusiInfo
     * @param map
     */
    public List<UpMchBusiInfo> passageValid(List<UpMchBusiInfo> upMchBusiInfo, Map<String, String> map) {
        // 符合通道的对象
        List<UpMchBusiInfo> mchBusiInfos = new ArrayList<>();

        for (UpMchBusiInfo mchBusiInfo : upMchBusiInfo) {
            // 获取当前时间
            DateTime date = DateUtil.date();
            int hour = DateUtil.hour(date, true);
            int minute = DateUtil.minute(date);
            //System.out.println("24 小时格式当前时间 >> " + hour + "时 " + minute + "分");
            // 将时分合并成一个
            double currentTime = Double.parseDouble(hour + "." + minute);

            /* --------- 时间区间 --------- */
            double openTime = Double.parseDouble(mchBusiInfo.getOpen_time());
            double closeTime = Double.parseDouble(mchBusiInfo.getClose_time());

            if (openTime > currentTime || currentTime > closeTime) {
                logger.error("通道未到开启时间 , 暂时无法交易");
                continue;
            }

            /* --------- 金额区间 --------- */
            int minAmount = Integer.parseInt(mchBusiInfo.getMin_amount());
            int maxAmount = Integer.parseInt(mchBusiInfo.getMax_amount());
            // 订单价格
            int totalFee = Integer.parseInt(map.get("total_fee"));

            if (minAmount > totalFee || totalFee > maxAmount) {
                logger.error("金额大小有误 , 无法交易");
                continue;
            }

            // 将符合的通道加入到集合中
            mchBusiInfos.add(mchBusiInfo);
        }
        return mchBusiInfos;
    }

    private Integer passageChoose(List<UpMchBusiInfo> upMchBusiInfos, Map<String, String> map) {
        Map<Integer, Integer> mapProfit = new HashMap<>();

        // 循环获取内容
        for (UpMchBusiInfo upMchBusiInfo : upMchBusiInfos) {
            // 交易金额
            BigDecimal totalFee = new BigDecimal(map.get("total_fee"));
            // 固定利润
            BigDecimal commisCharge = new BigDecimal(upMchBusiInfo.getMin_charge());
            // 百分比利润
            BigDecimal minCharge = new BigDecimal(upMchBusiInfo.getCommis_charge());
            // 计算公式 : 利润 = 交易金额 * 百分比利润 + 固定利润
            BigDecimal result = totalFee.multiply(commisCharge).add(minCharge);
            // 将计算完成的 利润 和通道 id 号放入 map 中
            mapProfit.put(upMchBusiInfo.getUp_busi_id(), result.intValue());
        }
        //System.out.println("利润计算完成" + ObjectUtil.toString(mapProfit));
        logger.info("利润计算完成"+mapProfit);
        // 得到最低利润
        Integer max = Collections.min(mapProfit.values());

        // 根据利润得到 通道 id , 并返回
        for (Map.Entry<Integer, Integer> entrySet : mapProfit.entrySet()) {
            if (entrySet.getValue().equals(max)) {
                return entrySet.getKey();
            }
        }
        // 没有通道返回 -1
        return -1;
    }

}