package com.shangfu.pay.reconciliation.reconciliation;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.google.gson.Gson;
import com.shangfu.pay.reconciliation.reconciliation.dao.DownSpInfoRespository;
import com.shangfu.pay.reconciliation.reconciliation.dao.UpReconInfoRepository;
import com.shangfu.pay.reconciliation.reconciliation.entity.DownSpInfo;
import com.shangfu.pay.reconciliation.reconciliation.entity.UpSpReconciliationInfo;
import com.shangfu.pay.reconciliation.reconciliation.util.RSAUtils;
import com.shangfu.pay.reconciliation.reconciliation.util.SignUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import java.io.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ReconciliationApplicationTests {

    @Autowired
    DownSpInfoRespository downSpInfoRespository;
    @Autowired
    private UpReconInfoRepository upReconInfoRepository;

    // 对账文件下载地址
    private String methodUrl = "http://testapi.shangfudata.com/gate/spsvr/trade/down";
    //private String refMethodUrl = "http://testapi.shangfudata.com/gate/spsvr/rfdtrade/down";
    private String signKey = "00000000000000000000000000000000";

    /**
     * 获取上游对账数据
     */
    @Test
    public void downloadForUp() {
        Map map = new HashMap();
        // 系统对账数据请求
        //map.put("down_sp_id", "1001");
        // 上游对账数据请求
        map.put("sp_id", "1000");
        // 指定日期 , 若未指定则使用上一个工作日期作为时间 .
        map.put("bill_date", "20190416");
        map.put("nonce_str", "123456789");
        map.put("sign", SignUtils.sign(map, signKey));

        Gson gson = new Gson();
        String s = gson.toJson(map);

        System.out.println("请求信息 > " + s);
        
        String post = HttpUtil.post(methodUrl, map);
        System.out.println("响应结果 > " + post);
    }

    /**
     * 推送系统对账数据
     */
    @Test
    public void downloadForDown() throws Exception {
        Gson gson = new Gson();

        DownSpInfo downSpInfo = downSpInfoRespository.findBySpId("1001");
        System.out.println(" >>> 签名 " + downSpInfo);

        //拿到密钥(私钥)
        String my_pri_key = downSpInfo.getDown_pri_key();
        RSAPrivateKey rsaPrivateKey;
        //拿到密钥(公钥)
        String down_pub_key = downSpInfo.getDown_pub_key();
        RSAPublicKey rsaPublicKey = null;

        rsaPrivateKey = RSAUtils.loadPrivateKey(my_pri_key);
        rsaPublicKey = RSAUtils.loadPublicKey(down_pub_key);

        Map map = new HashMap();
        // 系统对账数据请求
        map.put("down_sp_id", "1001");
        // 上游对账数据请求
        //map.put("sp_id", "1000");
        // 指定日期 , 若未指定则使用上一个工作日期作为时间 .
        map.put("bill_date", "20190409");
        map.put("nonce_str", "123456789");
        String s1 = gson.toJson(map);
        System.out.println("签名串 >>>> " + s1);
        map.put("sign", RSAUtils.sign(s1,rsaPrivateKey));

        String s = gson.toJson(map);
        System.out.println(s);

        String post = HttpUtil.post("http://localhost:8502/reconciliation/downloadToDown", s);
        System.out.println("响应结果 > " + post);
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

}