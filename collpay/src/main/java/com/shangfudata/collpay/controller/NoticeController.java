package com.shangfudata.collpay.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by tinlly to 2019/4/3
 * Package for com.shangfudata.collpay.controller
 */
@RestController
public class NoticeController {

    @PostMapping("/notice")
    @ResponseBody
    public void noticeMessage(@RequestBody String body){
        System.out.println("下游通知参数 > " + body);
    }

}
