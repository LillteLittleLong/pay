package com.shangfu.pay.reconciliation.reconciliation.service;

import java.io.IOException;

/**
 * Created by tinlly to 2019/4/9
 * Package for com.shangfu.reconciliation.pay.service
 */
public interface DownLoadFileService {

    /**
     * 下载文件保存为 txt
     */
    void downloadAndSaveFileTxt(String downloadInfoToJson) throws IOException;

    /**
     * 下载文件保存为 xlsx
     * @param downloadInfoToJson
     */
    void downloadAndSaveXlsx(String downloadInfoToJson);

    /**
     * 解析文件
     * @param filePath
     */
    void analysisFile(String filePath) throws IOException;

}
