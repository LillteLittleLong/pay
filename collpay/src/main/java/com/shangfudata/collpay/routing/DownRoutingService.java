package com.shangfudata.collpay.routing;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import com.shangfudata.collpay.dao.DownMchBusiInfoRepository;
import com.shangfudata.collpay.dao.DownRoutingInfoRepository;
import com.shangfudata.collpay.entity.CollpayInfo;
import com.shangfudata.collpay.entity.DownMchBusiInfo;
import com.shangfudata.collpay.entity.DownRoutingInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.*;

/**
 * Created by tinlly to 2019/4/1
 * Package for com.shangfu.pay.service.one
 */
@Service
public class DownRoutingService {

    @Autowired
    DownRoutingInfoRepository routingInfoRepository;
    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;

    /**
     * 下游路由分发处理
     */

    /**
     * 下游路由
     * 1. 下游发送请求
     * 2. 进行数据效验
     * 3. 路由指定通道
     * 3.1 有通道走通道
     * 3.2 无通道返回响应信息
     *
     * @param collpayInfo 下游请求信息
     * @return 返回可用通道
     */
    public List<DownMchBusiInfo> downRouting(CollpayInfo collpayInfo, Map<String, String> routingMap) {
        // 根据商户查询该商户通道
        // 查询数据库 , 判断是否有可走通道
        // 根据下游机构号和商户号查询路由表 , 查看是否有可用路由 .
        DownRoutingInfo downRoutingInfo = routingInfoRepository.queryOne(collpayInfo.getDown_mch_id(), collpayInfo.getDown_sp_id());

        System.out.println("路由表 downRoutingInfo >> " + ObjectUtil.toString(downRoutingInfo));

        // 如果没有通道
        if (null == downRoutingInfo) {
            Console.error("没有可用通道 , 无法交易");
            routingMap.put("status", "FAIL");
            routingMap.put("message", "没有可用通道 , 无法交易");
            return null;
        }

        // 获得了响应结果
        String downSpId = downRoutingInfo.getDown_sp_id();
        String downMchId = downRoutingInfo.getDown_mch_id();
        String passage = downRoutingInfo.getPassage();
        String mchId = downRoutingInfo.getMch_id();

        List<DownMchBusiInfo> downMchBusiInfo;

        // 获取该商户的所有通道
        if (mchId.equals("*")) {
            System.out.println("走 * 号通道");
            downMchBusiInfo = downMchBusiInfoRepository.queryMchPassage(downMchId, passage);
        } else {
            // 查询某个通道
            System.out.println("走单一通道");
            downMchBusiInfo = downMchBusiInfoRepository.queryMchPassage(downSpId, downMchId, passage);
        }
        System.out.println("通道业务表 downMchBusiInfo >> " + ObjectUtil.toString(downMchBusiInfo));

        // 通道验证是否可用 , 返回可用的通道
        List<DownMchBusiInfo> downMchBusiInfos = passageValid(downMchBusiInfo, collpayInfo);

        if (downMchBusiInfos.size() == 0) { // 如果没有可用通道
            Console.error("通道暂时不可用 , 无法交易");
            routingMap.put("status", "FAIL");
            routingMap.put("message", "通道暂时不可用 , 无法交易");
        }
        //else if (downMchBusiInfos.size() > 1) { // 如果有多个通道 , 选择利润最大的通道
        //    Set<String> strings = passageChoose(downMchBusiInfos, collpayInfo);
        //}

        System.out.println("下游可用通道有 > " + downMchBusiInfos.size());

        return downMchBusiInfos;
    }

    /**
     * 通道开关验证
     * @param downMchBusiInfo
     */
    public List<DownMchBusiInfo> passageValid(List<DownMchBusiInfo> downMchBusiInfo, CollpayInfo collpayInfo) {
        // 符合通道的对象
        List<DownMchBusiInfo> mchBusiInfos = new ArrayList<>();

        System.out.println(collpayInfo.getCard_name() + "的通道有 " + downMchBusiInfo.size());

        for (DownMchBusiInfo mchBusiInfo : downMchBusiInfo) {
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

    /**
     * 多通道优先级排序
     * 按照利润从大到小排序
     * @param downMchBusiInfos 可用通道
     * @param collpayInfo      下游请求数据
     */
    public Set<String> passageChoose(List<DownMchBusiInfo> downMchBusiInfos, CollpayInfo collpayInfo) {
        Map<String, Integer> map = new TreeMap();

        // 循环获取内容
        for (DownMchBusiInfo downMchBusiInfo : downMchBusiInfos) {
            // 交易金额
            BigDecimal totalFee = new BigDecimal(collpayInfo.getTotal_fee());
            // 固定利润
            BigDecimal commisCharge = new BigDecimal(downMchBusiInfo.getMin_charge());
            // 百分比利润
            BigDecimal minCharge = new BigDecimal(downMchBusiInfo.getCommis_charge());
            // 计算公式 : 利润 = 交易金额 * 百分比利润 + 固定利润
            BigDecimal result = totalFee.multiply(commisCharge).add(minCharge);
            // 将计算完成的 利润 和通道 id 号放入 map 中
            map.put(downMchBusiInfo.getDown_busi_id(), result.intValue());
        }
        System.out.println("利润计算完成" + ObjectUtil.toString(map));

        // 返回排序后的路由
        return null;
    }
}