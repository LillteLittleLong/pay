package com.shangfu.pay.reconciliation.reconciliation.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.google.gson.Gson;
import com.shangfu.pay.reconciliation.reconciliation.dao.SysReconInfoRepository;
import com.shangfu.pay.reconciliation.reconciliation.dao.UpReconInfoRepository;
import com.shangfu.pay.reconciliation.reconciliation.entity.SysReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.service.DownSpDownLoadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service.impl
 */
@Service
public class DownSpDownLoadFileServiceImpl implements DownSpDownLoadFileService {

    @Autowired
    UpReconInfoRepository upReconInfoRepository;
    @Autowired
    SysReconInfoRepository sysReconInfoRepository;

    /**
     * 系统下载下游对账文件
     * 下游根据时间获取 对账正确的 系统对账信息
     */
    @Override
    public String downloadSysFile(String downloadFileJSON) {
        Gson gson = new Gson();

        // 数据转换为 Map
        Map downloadFileMap = gson.fromJson(downloadFileJSON, Map.class);
        String billDate = (String) downloadFileMap.get("bill_date");
        String downSpId = (String) downloadFileMap.get("down_sp_id");
        String nonceStr = (String) downloadFileMap.get("nonce_str");
        String sign = (String) downloadFileMap.get("sign");

        System.out.println("获取了 > " + downloadFileMap);
        // 时间
        //String trade_time = "2019040901620148";
        // 下游机构号
        //String down_sp_id = "1001";

        // TODO: 2019/4/10 签名验证 , 数据效验


        // 如果时间字段为空
        if(billDate == null){
            // 设置默认时间为当天前一天
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, -1); //得到前一天
            Date date = calendar.getTime();

            billDate = new SimpleDateFormat("yyyyMMdd").format(date);
        }else { // 时间字段不为空处理内容
            downloadFileMap.put("trade_time" , billDate.substring(0, 8));
            billDate = billDate.substring(0, 8);
        }

        System.out.println("处理后的字符串 > " + billDate);
        // 获取某个机构某天的对账 无误 的信息
        List<SysReconciliationInfo> sysReconciliationInfos = sysReconInfoRepository.findByTradeTimeAndSpId(billDate , downSpId , "true");

        // 将对账解析为字符串
        StringBuilder columnsBuilder = new StringBuilder();
        // 获取列名 , 将列名添加进字符串
        SysReconciliationInfo sysReconciliationInfo = new SysReconciliationInfo();
        sysReconciliationInfo.initNull();

        Map map = gson.fromJson(gson.toJson(sysReconciliationInfo), Map.class);
        // 去掉 sys_check_id 和 recon_state
        map.remove("trade_no");
        map.remove("sys_check_id");
        map.remove("recon_state");
        ArrayList objects = CollectionUtil.newArrayList(map.keySet().iterator());
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

        // 判断是否有对账数据
        if (sysReconciliationInfos.size() > 0) {
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
        }
        System.out.println("拼接字符串内容 > " + columnsBuilder);

        return columnsBuilder.toString();
    }
}