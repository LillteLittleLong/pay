package com.shangfu.pay.reconciliation.reconciliation.controller;

import com.shangfu.pay.reconciliation.reconciliation.service.UpSpDownLoadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * Created by tinlly to 2019/4/10
 * Package for com.shangfu.pay.reconciliation.reconciliation.controller
 */
@RestController
public class DownLoadController {

    @Autowired
    UpSpDownLoadFileService upSpDownLoadFileService;

    /**
     * 从上游下载对账文件
     *
     * @throws IOException
     */
    @PostMapping(value = "/shangfu/pay/reconciliation/download", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public void downloadByUpSp(@RequestBody String downloadJson) throws IOException {
        System.out.println("接收了请求参数 > " + downloadJson);
        upSpDownLoadFileService.downloadAndSaveFileTxt(downloadJson);
    }


    /**
     * 下载文件给下游
     */
    //@PostMapping("/shangfu/pay/reconciliation/download")
    @ResponseBody
    public void downloadToDownSp(@RequestBody String upSpReconciliationInfoJson) {

    }

}
