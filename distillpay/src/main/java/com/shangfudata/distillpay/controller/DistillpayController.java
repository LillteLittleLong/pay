package com.shangfudata.distillpay.controller;

import com.shangfudata.distillpay.service.DistillpayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distillpay")
public class DistillpayController {

    @Autowired
    DistillpayService distillpayService;

    /**
     * 对下开放请求内部处理接口
     * @param distillpayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/trading", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Distillpay(@RequestBody String distillpayInfoToJson) throws Exception{

        return distillpayService.downDistillpay(distillpayInfoToJson);

    }

}
