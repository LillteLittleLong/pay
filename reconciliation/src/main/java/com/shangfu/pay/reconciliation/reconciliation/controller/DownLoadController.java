package com.shangfu.pay.reconciliation.reconciliation.controller;

import com.shangfu.pay.reconciliation.reconciliation.service.DownLoadFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * Created by tinlly to 2019/4/10
 * Package for com.shangfu.pay.reconciliation.reconciliation.controller
 */
@RestController
public class DownLoadController {

    @Autowired
    DownLoadFileService recontiliationService;

    @PostMapping("/shangfu/pay/reconciliation/download")
    @ResponseBody
    public void downLoad(@RequestBody String upSpReconciliationInfoJson) throws IOException {
        recontiliationService.downloadAndSaveFileTxt(upSpReconciliationInfoJson);
    }

}
