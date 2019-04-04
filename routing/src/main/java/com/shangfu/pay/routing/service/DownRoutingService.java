package com.shangfu.pay.routing.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.util.ObjectUtil;
import com.shangfu.pay.routing.dao.DownMchBusiInfoRepository;
import com.shangfu.pay.routing.entity.DownMchBusiInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by tinlly to 2019/4/1
 * Package for com.shangfu.pay.service.one
 */
@Service
public class DownRoutingService {

    @Autowired
    DownMchBusiInfoRepository downMchBusiInfoRepository;

    /**
     * 下游路由分发处理
     */

    /**
     * 下游路由
     * 1. 下游发送请求
     * 2. 进行数据效验
     * 3. 查看是否有通道
     * 3.1 验证通道是否可用
     *
     * @param map 下游请求信息
     * @return 返回通道 id
     */
    public Integer downRouting(Map<String, String> map, Map<String, String> routingMap) {
        System.out.println("请求参数 >> " + ObjectUtil.toString(map));

        // 获取该商户的所有通道
        // 查询某个通道
        DownMchBusiInfo downMchBusiInfo = downMchBusiInfoRepository.queryMchPassage(map.get("down_sp_id"), map.get("down_mch_id"), map.get("passage"));
        System.out.println("通道业务表 downMchBusiInfo >> " + ObjectUtil.toString(downMchBusiInfo));


        if (null == downMchBusiInfo) {
            routingMap.put("status", "FAIL");
            routingMap.put("message", "没有可用通道 , 无法交易");
        }

        // 通道验证是否可用 , 返回可用的通道
        Integer downBusiId = passageValid(downMchBusiInfo, map);

        if (-1 == downBusiId) {
            routingMap.put("status", "FAIL");
            routingMap.put("message", "通道暂时不可用 , 无法交易");
        }

        return downBusiId;
    }

    /**
     * 通道是否可用验证
     *
     * @param downMchBusiInfo
     */
    public Integer passageValid(DownMchBusiInfo downMchBusiInfo, Map<String, String> map) {
        // 获取当前时间
        DateTime date = DateUtil.date();
        int hour = DateUtil.hour(date, true);
        int minute = DateUtil.minute(date);
        System.out.println("24 小时格式当前时间 >> " + hour + "时 " + minute + "分");
        // 将时分合并成一个
        double currentTime = Double.parseDouble(hour + "." + minute);

        /* --------- 时间区间 --------- */
        double openTime = Double.parseDouble(downMchBusiInfo.getOpen_time());
        double closeTime = Double.parseDouble(downMchBusiInfo.getClose_time());

        if (openTime > currentTime || currentTime > closeTime) {
            Console.error("通道未到开启时间 , 暂时无法交易");
            map.put("status", "FAIL");
            map.put("message", "通道未到开启时间 , 暂时无法交易");
            return -1;
        }

        /* --------- 金额区间 --------- */
        int minAmount = Integer.parseInt(downMchBusiInfo.getMin_amount());
        int maxAmount = Integer.parseInt(downMchBusiInfo.getMax_amount());
        // 订单价格
        int totalFee = Integer.parseInt(map.get("total_fee"));

        if (minAmount > totalFee || totalFee > maxAmount) {
            Console.error("金额大小有误 , 无法交易");
            map.put("status", "FAIL");
            map.put("message", "金额大小有误 , 无法交易");
            return -1;
        }
        // 将符合的通道加入到集合中
        return downMchBusiInfo.getDown_busi_id();
    }

}