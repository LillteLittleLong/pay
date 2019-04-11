package com.shangfudata.collpay.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Console;
import com.google.gson.Gson;
import com.shangfudata.collpay.dao.SysReconInfoRepository;
import com.shangfudata.collpay.dao.UpReconInfoRepository;
import com.shangfudata.collpay.entity.SysReconciliationInfo;
import com.shangfudata.collpay.entity.UpReconciliationInfo;
import com.shangfudata.collpay.service.ReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service.impl
 */
@Service
public class ReconServiceImpl implements ReconciliationService {

    @Autowired
    UpReconInfoRepository upReconInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;

    /**
     * 对账
     */
    public void reconciliationInfo() {
        upReconciliationSys();
        sysReconcilitionUp();
    }


    /**
     * 1. 获取所有上游对账信息
     * 2. 以上游对账信息为主对比系统订单
     */
    @Override
    public void upReconciliationSys() {
        System.out.println("开始以上游对账表为主比较系统订单表");
        List<UpReconciliationInfo> all = upReconInfoRepository.queryUpReconciliationByTradeType("CP_PAY");
        for (UpReconciliationInfo upReconciliationInfo : all) {
            // 以上游订单为主对比系统订单
            SysReconciliationInfo sysReconciliationInfo = sysReconInfoRepository.findByChTradeNo(upReconciliationInfo.getTrade_no());

            System.out.println(sysReconciliationInfo);
            System.out.println(upReconciliationInfo);

            if (null == sysReconciliationInfo) {
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                continue;
            }

            // 商户通道比较
            if (!(sysReconciliationInfo.getSp_trade_no().equals(upReconciliationInfo.getSp_trade_no()))) {
                Console.error("不是同一个商户");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // TODO: 2019/4/10 对账错误处理
                continue;
            }
            // 机构通道比较
            if (!(sysReconciliationInfo.getTrade_no().equals(upReconciliationInfo.getTrade_no()))) {
                Console.error("不是同一个机构");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // TODO: 2019/4/10 对账错误处理
                continue;
            }
            // 交易状态比较
            if (!(sysReconciliationInfo.getTrade_state().equals(upReconciliationInfo.getTrade_state()))) {
                Console.error("金额不对");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // TODO: 2019/4/10 对账错误处理
                continue;
            }
            // 总金额比较
            if (!(sysReconciliationInfo.getTotal_fee().equals(upReconciliationInfo.getTotal_fee()))) {
                Console.error("金额不对");
                upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                // TODO: 2019/4/10 对账错误处理
                continue;
            }
            // TODO: 2019/4/9 利润比较

            // 当对账成功时改变状态
            upReconInfoRepository.updateReconStateByTradeNo("true", upReconciliationInfo.getTrade_no());
        }
    }

    /**
     * 1. 获取所有系统订单信息和上游对账信息
     * 2. 以系统订单为主对比上游对账信息
     */
    @Override
    public void sysReconcilitionUp() {
        List<SysReconciliationInfo> sysReconciliationInfos = sysReconInfoRepository.findAll();
        for (SysReconciliationInfo sysReconciliationInfo : sysReconciliationInfos) {
            // 获取某个通道的内容
            List<UpReconciliationInfo> upReconciliationInfos = upReconInfoRepository.queryUpReconciliationByTradeType("CP_PAY");
            for (UpReconciliationInfo upReconciliationInfo : upReconciliationInfos) {
                // 商户通道比较
                if (!(sysReconciliationInfo.getSp_trade_no()).equals(upReconciliationInfo.getSp_trade_no())) {
                    Console.error("不是同一个商户");
                    upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                    // TODO: 2019/4/10 对账错误处理
                    continue;
                }
                // 机构通道比较
                if (!(sysReconciliationInfo.getTrade_no().equals(upReconciliationInfo.getTrade_no()))) {
                    Console.error("不是同一个机构");
                    upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                    // TODO: 2019/4/10 对账错误处理
                    continue;
                }
                // 交易状态比较
                if (!(sysReconciliationInfo.getTrade_state().equals(upReconciliationInfo.getTrade_state()))) {
                    Console.error("金额不对");
                    upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                    // TODO: 2019/4/10 对账错误处理
                    continue;
                }
                // 总金额比较
                if (!(sysReconciliationInfo.getTotal_fee().equals(upReconciliationInfo.getTotal_fee()))) {
                    Console.error("金额不对");
                    upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                    // TODO: 2019/4/10 对账错误处理
                    continue;
                }

                // TODO: 2019/4/9 利润比较
                if (!(sysReconciliationInfo.getHand_fee().equals(upReconciliationInfo.getHand_fee()))) {
                    upReconInfoRepository.updateReconStateByTradeNo("false", upReconciliationInfo.getTrade_no());
                    // TODO: 2019/4/10 对账错误处理
                    continue;
                }

                // 当对账成功时改变状态
                upReconInfoRepository.updateReconStateByTradeNo("true", upReconciliationInfo.getTrade_no());
            }
        }
    }

    /**
     * 系统下载下游对账文件
     */
    @Override
    public String downloadSysFile() {
        // TODO: 2019/4/10 签名验证 , 数据效验

        // TODO: 2019/4/10 添加时间筛选功能
        // 时间
        String trade_time = "2019040901620148";
        // 下游机构号
        String down_sp_id = "1001";

        // TODO: 2019/4/10 设置时间字段缺省默认值
        String tradeTime;
        // 如果时间字段为空
        if(trade_time == null){
            // 设置默认时间为当天前一天
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1); //得到前一天
            Date date = calendar.getTime();
            tradeTime = new SimpleDateFormat("YYYYMMdd").format(date);
        }else { // 时间字段不为空处理内容
            tradeTime = trade_time.substring(0, 8);
        }
        System.out.println("处理后的字符串 > " + tradeTime);
        // 获取某个机构某天的对账信息
        List<SysReconciliationInfo> sysReconciliationInfos = sysReconInfoRepository.findByTradeTimeAndSpId(tradeTime , down_sp_id);

        if (sysReconciliationInfos.size() == 0){
            return "没有数据";
        }

        // 将对账解析为字符串
        Gson gson = new Gson();
        StringBuilder columnsBuilder = new StringBuilder();
        // 获取列名 , 将列名添加进字符串
        SysReconciliationInfo sysReconciliationInfo = sysReconciliationInfos.get(0);
        Map map = gson.fromJson(gson.toJson(sysReconciliationInfo), Map.class);
        // 去掉 sys_check_id 和 recon_state
        map.remove("trade_no");
        map.remove("sys_check_id");
        map.remove("recon_state");
        ArrayList<String> objects = CollectionUtil.newArrayList(map.keySet().iterator());
        // 添加 column 值
        for (int index = 0; index < objects.size(); index++) {
            columnsBuilder.append(objects.get(index));
            // 如果 recon_state
            if (objects.get(index).equals("recon_state")) {
                continue;
            }
            if (index != objects.size() - 1) {
                columnsBuilder.append(",");
            }
        }
        columnsBuilder.append("\n");

        // 通过对象添加内容
        for (SysReconciliationInfo reconciliationInfo : sysReconciliationInfos) {
            columnsBuilder.append(reconciliationInfo.getTrade_time()).append(",");
            columnsBuilder.append(reconciliationInfo.getTrade_state()).append(",");
            columnsBuilder.append(reconciliationInfo.getTotal_fee()).append(",");
            columnsBuilder.append(reconciliationInfo.getHand_fee()).append(",");
            columnsBuilder.append(reconciliationInfo.getTrade_type()).append(",");
            columnsBuilder.append(reconciliationInfo.getSp_trade_no()).append(",");
            columnsBuilder.append(reconciliationInfo.getDown_mch_id()).append(",");
            columnsBuilder.append(reconciliationInfo.getDown_sp_id()).append(",");
            columnsBuilder.append(reconciliationInfo.getDown_charge()).append("\n");
        }

        return columnsBuilder.toString();
    }
}