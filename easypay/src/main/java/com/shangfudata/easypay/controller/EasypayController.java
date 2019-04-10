package com.shangfudata.easypay.controller;

import com.shangfudata.easypay.service.EasypayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/easypay")
public class EasypayController {

    @Autowired
    EasypayService easypayService;

    /**
     * 对下开放  下单  内部处理接口
     * @param EasypayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/trading", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Easypay(@RequestBody String EasypayInfoToJson) throws Exception{


        return easypayService.downEasypay(EasypayInfoToJson);

    }

}
