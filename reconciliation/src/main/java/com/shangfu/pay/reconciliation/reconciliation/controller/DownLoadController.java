package com.shangfu.pay.reconciliation.reconciliation.controller;

import com.shangfu.pay.reconciliation.reconciliation.service.DownSpDownLoadFileService;
import com.shangfu.pay.reconciliation.reconciliation.service.UpSpDownLoadFileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by tinlly to 2019/4/10
 * Package for com.shangfu.pay.reconciliation.reconciliation.controller
 */
@RestController
@RequestMapping("/reconciliation")
public class DownLoadController {

    Logger logger = LoggerFactory.getLogger(this.getClass());


    @Autowired
    UpSpDownLoadFileService upSpDownLoadFileService;
    @Autowired
    DownSpDownLoadFileService downSpDownLoadFileService;

    /**
     * 从上游下载对账文件
     */
    @PostMapping(value = "/downloadToUp", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public void downloadByUpSp(@RequestBody String downloadJson) {
        logger.info("对账接收请求参数 > " + downloadJson);
        upSpDownLoadFileService.downloadAndSaveFileTxt(downloadJson);
    }

    /**
     * 下载文件给下游
     */
    @PostMapping(value = "/downloadToDown", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String downloadToDownSp(@RequestBody String upSpReconciliationInfoJson) {
        return downSpDownLoadFileService.downloadSysFile(upSpReconciliationInfoJson);
    }

}
