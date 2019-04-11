package com.shangfudata.collpay.controller;

import com.shangfudata.collpay.service.ReconciliationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.jnlp.DownloadService;

/**
 * Created by tinlly to 2019/4/10
 * Package for com.shangfudata.collpay.controller
 */
@RestController
@RequestMapping("/collpay")
public class ReconController {

    @Autowired
    ReconciliationService reconciliationService;

    /**
     * 对账方法
     */
    @PostMapping("/recon")
    @ResponseBody
    public void reconCheck() {
        System.out.println("准备进入 reconciliationInfo 方法");
        reconciliationService.reconciliationInfo();
    }

    /**
     * 下游机构下载文件
     */
    @PostMapping("/downloadSysFile")
    @ResponseBody
    public String downSpDownloadSysFile(){
        String s = reconciliationService.downloadSysFile();
        System.out.println("拼接后的字符串 > " + s);
        return s;
    }

}
