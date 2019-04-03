package com.shangfudata.collpay.controller;

import com.shangfudata.collpay.service.impl.CollpayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/collpay")
public class CollpayController {

    @Autowired
    CollpayServiceImpl collpayService;


    /**
     * 对下开放请求内部处理接口
     * @param CollpayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/trading", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Collpay(@RequestBody String CollpayInfoToJson) throws Exception{
        System.out.println("接收了请求 > " + CollpayInfoToJson);
        return collpayService.downCollpay(CollpayInfoToJson);

    }




}
