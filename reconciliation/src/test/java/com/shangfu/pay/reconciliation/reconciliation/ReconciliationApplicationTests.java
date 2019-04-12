package com.shangfu.pay.reconciliation.reconciliation;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.google.gson.Gson;
import com.shangfu.pay.reconciliation.reconciliation.dao.UpReconInfoRepository;
import com.shangfu.pay.reconciliation.reconciliation.entity.SysReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.entity.UpSpReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.util.SignUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.*;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReconciliationApplicationTests {

    @Autowired
    private UpReconInfoRepository upReconInfoRepository;

    // 对账文件下载地址
    private String methodUrl = "http://testapi.shangfudata.com/gate/spsvr/trade/down";
    //private String refMethodUrl = "http://testapi.shangfudata.com/gate/spsvr/rfdtrade/down";
    private String signKey = "00000000000000000000000000000000";

    @Test
    public void downloadForTxt() {
        Map map = new HashMap();
        // 系统对账数据请求
        map.put("down_sp_id", "1001");
        // 上游对账数据请求
        //map.put("sp_id", "1000");
        // 指定日期 , 若未指定则使用上一个工作日期作为时间 .
        map.put("bill_date", "20190409");
        map.put("nonce_str", "123456789");
        map.put("sign", SignUtils.sign(map, signKey));

        Gson gson = new Gson();
        String s = gson.toJson(map);

        System.out.println("请求信息 > " + s);

        String post = HttpUtil.post(methodUrl, map);
        System.out.println("响应结果 > " + post);
        //FileWriter fileWriter = null;
        //BufferedWriter bufferedWriter = null;
        //// 将上游对账文件写入 txt 文件
        //try {
        //    fileWriter = new FileWriter("C:\\Users\\shangfu222\\Desktop\\对账文件\\reconciliation\\txt\\" + DateUtil.formatDate(new Date()) + System.currentTimeMillis() + "download.txt");
        //    bufferedWriter = new BufferedWriter(fileWriter);
        //    bufferedWriter.write(post);
        //    bufferedWriter.flush();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //} finally {
        //    bufferedWriter.close();
        //    fileWriter.close();
        //}
        //Gson gson = new Gson();
        //
        //// 存入多个 Map 的集合
        //FileReader fileReader = null;
        //BufferedReader bufferedReader = null;
        //try {
        //    // 解析文件
        //    // 将文件解析为对象
        //    fileReader = new FileReader("C:\\Users\\shangfu222\\Desktop\\对账文件\\reconciliation\\txt\\2019-04-09download.txt");
        //    bufferedReader = new BufferedReader(fileReader);
        //    // 读取第一行 : 列名
        //    String string = bufferedReader.readLine();
        //    String[] split = string.split(",");
        //
        //    // 存入
        //    Map columnMap = new HashMap();
        //    for (int index = 0; index < split.length; index++) {
        //        columnMap.put(split[index], "");
        //    }
        //
        //    // 列名效验
        //    while (!((string = bufferedReader.readLine()) == null)) {
        //        String[] column = string.split(",");
        //        System.out.println("数组 > " + Arrays.toString(column));
        //        for (int index = 0; index < column.length; index++) {
        //            columnMap.put(split[index], column[index]);
        //        }
        //        UpSpReconciliationInfo upSpReconciliationInfo = gson.fromJson(gson.toJson(columnMap), UpSpReconciliationInfo.class);
        //        upSpReconciliationInfoRepository.save(upSpReconciliationInfo);
        //    }
        //} catch (IOException e) {
        //    e.printStackTrace();
        //} finally {
        //    bufferedReader.close();
        //    fileReader.close();
        //}
    }

    @Test
    public void downloadForXlsx() {
        Map map = new HashMap();
        // 获取机构下的所有商户订单
        map.put("sp_id", "1000");
        // 指定日期 , 若未指定则使用上一个工作日期作为时间 .
        //map.put("bill_date" , "20190408");
        map.put("nonce_str", "123456789");
        map.put("sign", SignUtils.sign(map, signKey));
        String post = HttpUtil.post(methodUrl, map);
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
            //upReconInfoRepository.save(upSpReconciliationInfo);
        }
        reader.close();
    }

    @Test
    public void downloadForTxtRefund() throws IOException {
        //Map map = new HashMap();
        //// 获取机构下的所有商户订单
        //map.put("sp_id", "1000");
        //// 指定日期 , 若未指定则使用上一个工作日期作为时间 .
        //map.put("bill_date", "20190402");
        //map.put("nonce_str", "123456789");
        //map.put("sign", SignUtils.sign(map, signKey));
        //String post = HttpUtil.post(refMethodUrl, map);
        //
        //System.out.println("请求内容 > " + post);
        //
        //FileWriter fileWriter = null;
        //BufferedWriter bufferedWriter = null;
        //// 将上游对账文件写入 txt 文件
        //try {
        //    fileWriter = new FileWriter("C:\\Users\\shangfu222\\Desktop\\对账文件\\reconciliation\\txt\\" + DateUtil.formatDate(new Date()) + System.currentTimeMillis() + "rfdDownload.txt");
        //    bufferedWriter = new BufferedWriter(fileWriter);
        //    bufferedWriter.write(post);
        //    bufferedWriter.flush();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //} finally {
        //    bufferedWriter.close();
        //    fileWriter.close();
        //}
    }

    @Test
    public void test(){
        Gson gson = new Gson();
        String s = gson.toJson(new SysReconciliationInfo());
        System.out.println(s);
    }

}