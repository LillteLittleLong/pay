package com.shangfudata.gatewaypay.controller;

import com.shangfudata.gatewaypay.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/gatewaypay")
public class NoticeController {

    @Autowired

    NoticeService noticeService;


    @PostMapping("/notice")
    @ResponseBody
    public String notice(@RequestParam Map<String ,String> map){
        //传入订单号以及交易状态
        return noticeService.Upnotice(map);
    }
}
