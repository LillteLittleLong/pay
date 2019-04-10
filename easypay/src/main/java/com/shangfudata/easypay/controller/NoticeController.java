package com.shangfudata.easypay.controller;

import com.shangfudata.easypay.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/easypay")
public class NoticeController {

    @Autowired
    NoticeService noticeService;


    @PostMapping("/notice")
    @ResponseBody
    public String notice(@RequestParam Map<String,String> map){

        //传入订单号以及交易状态
        return noticeService.Upnotice(map);
    }
}
