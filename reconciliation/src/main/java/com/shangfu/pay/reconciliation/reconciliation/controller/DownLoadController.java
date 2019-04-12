package com.shangfu.pay.reconciliation.reconciliation.controller;

import com.shangfu.pay.reconciliation.reconciliation.service.DownSpDownLoadFileService;
import com.shangfu.pay.reconciliation.reconciliation.service.UpSpDownLoadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Created by tinlly to 2019/4/10
 * Package for com.shangfu.pay.reconciliation.reconciliation.controller
 */
@RestController
@RequestMapping("/reconciliation")
public class DownLoadController {

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
        System.out.println("接收了请求参数 > " + downloadJson);
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
