package com.shangfudata.easypay.controller;

import com.shangfudata.easypay.service.NoticeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/easypay")
public class NoticeController {

    @Autowired
    NoticeService noticeService;
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostMapping("/notice")
    @ResponseBody
    public String notice(@RequestParam Map<String ,String> map){

        noticeService.Upnotice(map);
        //传入订单号以及交易状态
        return "SUCCESS";
    }
}
