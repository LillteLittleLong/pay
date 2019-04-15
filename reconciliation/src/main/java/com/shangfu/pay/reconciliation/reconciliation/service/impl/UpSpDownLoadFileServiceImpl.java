package com.shangfu.pay.reconciliation.reconciliation.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.google.gson.Gson;
import com.shangfu.pay.reconciliation.reconciliation.dao.UpReconInfoRepository;
import com.shangfu.pay.reconciliation.reconciliation.entity.UpReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.entity.UpSpReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.service.UpSpDownLoadFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service.impl
 */
@Service
public class UpSpDownLoadFileServiceImpl implements UpSpDownLoadFileService {

    /**
     * 上游对账信息
     */

    @Autowired
    private UpReconInfoRepository upReconInfoRepository;
    private String methodUrl = "http://testapi.shangfudata.com/gate/spsvr/trade/down";
    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 从上游获取请求文件 , 保存到本地 txt 格式文件 .
     */
    @Override
    public void downloadAndSaveFileTxt(String downloadJson) {
        // TODO: 2019/4/10 数据效验

        Gson gson = new Gson();
        Map map = gson.fromJson(downloadJson, Map.class);

        String bill_date = (String)map.get("bill_date");
        if (null == bill_date && bill_date.equals("")) {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);
            calendar.add(calendar.DATE, -1);
            date = calendar.getTime();
            String tradeTime = new SimpleDateFormat("yyyyMMdd").format(date);
            map.put("bill_date", tradeTime);
        }

        // 发送请求 , 得到响应结果
        String responseBody = HttpRequest.post(methodUrl).form(map).execute().body();
        logger.info("从上游得到的对账响应数据 > " + responseBody);

        // TODO: 2019/4/12 方法加强
        // 如果请求失败 , 不执行方法
        if (responseBody.length() < 10) {
            return;
        }

        // 将请求结果持久化到本地
        File file = new File("C:\\Users\\shangfu222\\Desktop\\对账文件\\reconciliation\\txt\\" + DateUtil.formatDate(new Date()) + System.currentTimeMillis() + "download.txt");
        // 解析响应信息 , 保存到本地 txt 文件
        try (FileWriter fileWriter = new FileWriter(file); BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
            bufferedWriter.write(responseBody);
            bufferedWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        logger.info("保存的文件路径以及文件名 > " + file.toString());
        // 执行解析方法
        analysisTxtFile(file.toString());
    }

    /**
     * 解析本地 txt 文件 , 保存到数据库
     *
     * @param filePath
     */
    @Override
    public void analysisTxtFile(String filePath) {
        Gson gson = new Gson();
        try (FileReader fileReader = new FileReader(filePath); BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            // 解析第一行获取列名
            String string = bufferedReader.readLine();
            String[] split = string.split(",");

            // 解析字段名
            Map columnMap = new HashMap();
            for (int index = 0; index < split.length; index++) {
                columnMap.put(split[index], "");
            }

            // 解析字段值
            while (!((string = bufferedReader.readLine()) == null)) {
                String[] column = string.split(",");
                // 将每列的字段值存入到 Map 对应的 key value 中
                for (int index = 0; index < column.length; index++) {
                    columnMap.put(split[index], column[index]);
                }
                // 包装为对象
                UpSpReconciliationInfo upSpReconciliationInfo = gson.fromJson(gson.toJson(columnMap), UpSpReconciliationInfo.class);

                // 排除 CP_PAY 以及 DISTILL_PAY 以外的业务
                if (!(upSpReconciliationInfo.getTrade_type().equals("CP_PAY") || upSpReconciliationInfo.getTrade_type().equals("DISTILL_PAY"))) {
                    return;
                }

                UpReconciliationInfo upReconciliationInfo = new UpReconciliationInfo();
                upReconciliationInfo.setSp_id(upSpReconciliationInfo.getSp_id());
                upReconciliationInfo.setMch_id(upSpReconciliationInfo.getMcht_no());
                upReconciliationInfo.setTrade_time(upSpReconciliationInfo.getTrade_time());
                upReconciliationInfo.setTrade_state(upSpReconciliationInfo.getTrade_state());
                upReconciliationInfo.setTotal_fee(upSpReconciliationInfo.getTotal_fee());
                upReconciliationInfo.setHand_fee(upSpReconciliationInfo.getHand_fee());
                upReconciliationInfo.setTrade_type(upSpReconciliationInfo.getTrade_type());
                upReconciliationInfo.setSp_trade_no(upSpReconciliationInfo.getSp_trade_no());
                upReconciliationInfo.setTrade_no(upSpReconciliationInfo.getTrade_no());

                logger.info("保存到上游对账表的数据 > " + upReconciliationInfo);
                // 保存到数据库
                upReconInfoRepository.save(upReconciliationInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从上游获取请求文件 , 保存到本地 xlsx 格式文件 .
     * @param downloadInfoToJson
     */
    @Override
    public void downloadAndSaveFileXlsx(String downloadInfoToJson) {
        String post = HttpUtil.post(methodUrl, downloadInfoToJson, 12000);
        logger.info("响应结果 >> " + post);
        File file = new File("C:\\Users\\shangfu222\\Desktop\\对账文件\\reconciliation\\xlsx\\" + DateUtil.formatDate(new Date()) + System.currentTimeMillis() + "download.xlsx");

        // 获取 ExcelWriter 对象
        try (ExcelWriter writer = ExcelUtil.getWriter(file)) {
            // 解析响应信息 , 保存到本地 xlsx 文件
            String[] split = post.split("\r\n");
            logger.info("数组 > " + Arrays.toString(split));

            for (int index = 0; index < split.length; index++) {
                String[] columnMap = split[index].split(",");
                List<String> strings = Arrays.asList(columnMap);
                writer.writeRow(strings, false);
                writer.flush();
            }
        } catch (IORuntimeException e) {
            e.printStackTrace();
        }

        // 解析文件
        analysisXlsxFile(file.getPath());
    }


    /**
     * 解析本地 xlsx 文件 , 保存到数据库
     * @param filePath
     */
    @Override
    public void analysisXlsxFile(String filePath) {
        logger.info("解析 Excel 文件");
        // TODO: 2019/4/9 解析文件
        // 获取 ExcelReader 对象
        try (ExcelReader reader = ExcelUtil.getReader(filePath)) {
            List<UpSpReconciliationInfo> infoList = reader.readAll(UpSpReconciliationInfo.class);
            for (UpSpReconciliationInfo upSpReconciliationInfo : infoList) {
                UpReconciliationInfo upReconciliationInfo = new UpReconciliationInfo();
                upReconciliationInfo.setTrade_time(upSpReconciliationInfo.getTrade_time());
                upReconciliationInfo.setTrade_state(upSpReconciliationInfo.getTrade_state());
                upReconciliationInfo.setTotal_fee(upSpReconciliationInfo.getTotal_fee());
                upReconciliationInfo.setHand_fee(upSpReconciliationInfo.getHand_fee());
                upReconciliationInfo.setTrade_type(upSpReconciliationInfo.getTrade_type());
                upReconciliationInfo.setSp_trade_no(upSpReconciliationInfo.getSp_trade_no());
                upReconciliationInfo.setTrade_no(upSpReconciliationInfo.getSp_trade_no());

                upReconInfoRepository.save(upReconciliationInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}