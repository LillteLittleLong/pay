package com.shangfudata.distillpay.controller;

import com.shangfudata.distillpay.service.DistillpayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distillpay")
public class DistillpayController {

    @Autowired
    DistillpayService distillpayService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 对下开放请求内部处理接口
     * @param distillpayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/trading", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Distillpay(@RequestBody String distillpayInfoToJson) throws Exception{

        logger.info("接口接收下游交易请求信息："+distillpayInfoToJson);

        return distillpayService.downDistillpay(distillpayInfoToJson);

    }

}
