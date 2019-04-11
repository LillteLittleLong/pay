package com.shangfu.pay.reconciliation.reconciliation.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.google.gson.Gson;
import com.shangfu.pay.reconciliation.reconciliation.dao.UpReconInfoRepository;
import com.shangfu.pay.reconciliation.reconciliation.entity.UpReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.entity.UpSpReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.service.DownLoadFileService;
import com.shangfu.pay.reconciliation.reconciliation.util.SignUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service.impl
 */
@Service
public class UpReconFileServiceImpl implements DownLoadFileService {

    /**
     * 上游对账信息
     */

    @Autowired
    private UpReconInfoRepository upReconInfoRepository;

    private String methodUrl = "http://testapi.shangfudata.com/gate/spsvr/trade/down";
    private String signKey = "00000000000000000000000000000000";


    /**
     * 向上游发送请求数据保存到数据库
     *
     * @param downloadInfoToJson
     */
    @Override
    public void downloadAndSaveFileTxt(String downloadInfoToJson) {
        System.out.println("下游请求数据 json > " + downloadInfoToJson);
        Gson gson = new Gson();

        // TODO: 2019/4/10 数据效验
        // TODO: 2019/4/10 if(){}else{}

        Map map = new HashMap();
        // 获取机构下的所有商户订单
        map.put("sp_id", "1000");
        // 指定日期 , 若未指定则使用上一个工作日期作为时间 .
        map.put("bill_date", "20190409");
        map.put("nonce_str", "123456789");
        map.put("sign", SignUtils.sign(map, signKey));
        // 发送请求
        String post = HttpUtil.post(methodUrl, map);
        System.out.println("响应结果 > " + post);

        // TODO: 2019/4/10 验证请求结果
        // TODO: 2019/4/10 if(){}else{}

        // 将请求结果持久化到本地
        File file = new File("C:\\Users\\shangfu222\\Desktop\\对账文件\\reconciliation\\txt\\" + DateUtil.formatDate(new Date()) + System.currentTimeMillis() + "download.txt");
        try (FileWriter fileWriter = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            // 写入文件
            bufferedWriter.write(post);
            // 刷新缓冲区
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("file 获取内容 > " + file.toString());
        // 执行解析方法
        analysisFile(file.toString());
    }

    /**
     * 解析文件 , 保存到数据库
     *
     * @param filePath
     */
    @Override
    public void analysisFile(String filePath) {
        Gson gson = new Gson();
        try (FileReader fileReader = new FileReader(filePath); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            // 解析第一行获取列名
            String string = bufferedReader.readLine();
            String[] split = string.split(",");

            // 将列名存入 map 中作为 key
            Map columnMap = new HashMap();
            for (int index = 0; index < split.length; index++) {
                columnMap.put(split[index], "");
            }

            // 解析剩余内容
            while (!((string = bufferedReader.readLine()) == null)) {
                String[] column = string.split(",");
                // 将每列的字段值存入到 Map 对应的 key value 中
                for (int index = 0; index < column.length; index++) {
                    columnMap.put(split[index], column[index]);
                }
                // 包装为对象
                UpSpReconciliationInfo upSpReconciliationInfo = gson.fromJson(gson.toJson(columnMap), UpSpReconciliationInfo.class);

                // 保存到数据库
                UpReconciliationInfo upReconciliationInfo = new UpReconciliationInfo();
                upReconciliationInfo.setUp_check_id(System.currentTimeMillis() + "");
                upReconciliationInfo.setTrade_time(upSpReconciliationInfo.getTrade_time());
                upReconciliationInfo.setTrade_state(upSpReconciliationInfo.getTrade_state());
                upReconciliationInfo.setTotal_fee(upSpReconciliationInfo.getTotal_fee());
                upReconciliationInfo.setHand_fee(upSpReconciliationInfo.getHand_fee());
                upReconciliationInfo.setTrade_type(upSpReconciliationInfo.getTrade_type());
                upReconciliationInfo.setSp_trade_no(upSpReconciliationInfo.getSp_trade_no());
                upReconciliationInfo.setTrade_no(upSpReconciliationInfo.getSp_trade_no());

                //upReconciliationInfo.setTrade_time((String) columnMap.get("trade_time"));
                //upReconciliationInfo.setTrade_state((String) columnMap.get("trade_state"));
                //upReconciliationInfo.setTotal_fee((String) columnMap.get("total_fee"));
                //upReconciliationInfo.setHand_fee((String) columnMap.get("hand_fee"));
                //upReconciliationInfo.setTrade_type((String) columnMap.get("trade_type"));
                //upReconciliationInfo.setSp_trade_no((String) columnMap.get("sp_trade_no"));
                //upReconciliationInfo.setTrade_no((String) columnMap.get("trade_no"));

                upReconInfoRepository.save(upReconciliationInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void downloadAndSaveXlsx(String downloadInfoToJson) {
        String post = HttpUtil.post(methodUrl, downloadInfoToJson, 12000);
        System.out.println("响应结果 >> " + post);

        ExcelWriter writer = ExcelUtil.getWriter("C:\\Users\\shangfu222\\Desktop\\对账文件\\reconciliation\\xlsx\\" + DateUtil.formatDate(new Date()) + System.currentTimeMillis() + "download.xlsx");

        // 解析响应信息
        // 按照行分割
        String[] split = post.split("\r\n");
        System.out.println("数组 > " + Arrays.toString(split));

        for (int index = 0; index < split.length; index++) {
            String[] columnMap = split[index].split(",");
            List<String> strings = Arrays.asList(columnMap);
            writer.writeRow(strings, false);
            writer.flush();
        }
        writer.close();

        System.err.println("解析 Excel 文件");
        // TODO: 2019/4/9 解析文件
        // 解析存数据库
        ExcelReader reader = ExcelUtil.getReader("C:\\Users\\shangfu222\\Desktop\\对账文件\\reconciliation\\xlsx\\2019-04-091554780833212download.xlsx");

        List<UpSpReconciliationInfo> infoList = reader.readAll(UpSpReconciliationInfo.class);
        for (UpSpReconciliationInfo upSpReconciliationInfo : infoList) {
            UpReconciliationInfo upReconciliationInfo = new UpReconciliationInfo();
            upReconciliationInfo.setUp_check_id(System.currentTimeMillis() + "");
            upReconciliationInfo.setTrade_time(upSpReconciliationInfo.getTrade_time());
            upReconciliationInfo.setTrade_state(upSpReconciliationInfo.getTrade_state());
            upReconciliationInfo.setTotal_fee(upSpReconciliationInfo.getTotal_fee());
            upReconciliationInfo.setHand_fee(upSpReconciliationInfo.getHand_fee());
            upReconciliationInfo.setTrade_type(upSpReconciliationInfo.getTrade_type());
            upReconciliationInfo.setSp_trade_no(upSpReconciliationInfo.getSp_trade_no());
            upReconciliationInfo.setTrade_no(upSpReconciliationInfo.getSp_trade_no());

            upReconInfoRepository.save(upReconciliationInfo);
        }
        reader.close();
    }

}