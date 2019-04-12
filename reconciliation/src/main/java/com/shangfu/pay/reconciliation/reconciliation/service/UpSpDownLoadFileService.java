package com.shangfu.pay.reconciliation.reconciliation.service;

import java.io.IOException;
import java.util.Map;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service
 */
public interface UpSpDownLoadFileService {

    /**
     * 下载文件保存为 txt
     */
    void downloadAndSaveFileTxt(String downloadJson) throws IOException;


    /**
     * 解析 txt 文件 , 保存到数据库
     * @param filePath
     */
    void analysisTxtFile(String filePath) throws IOException;

    /**
     * 下载文件保存为 xlsx
     * @param downloadInfoToJson
     */
    void downloadAndSaveFileXlsx(String downloadInfoToJson);

    /**
     * 解析 xlsx 文件 , 保存到数据库
     * @param filePath
     */
    void analysisXlsxFile(String filePath) throws IOException;

}
