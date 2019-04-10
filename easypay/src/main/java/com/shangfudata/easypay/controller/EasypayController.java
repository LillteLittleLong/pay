package com.shangfudata.easypay.controller;

import com.shangfudata.easypay.service.EasypayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/easypay")
public class EasypayController {

    @Autowired
    EasypayService easypayService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 对下开放  下单  内部处理接口
     * @param EasypayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/trading", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Easypay(@RequestBody String EasypayInfoToJson) throws Exception{

        logger.info("接口接收下游下单请求信息："+EasypayInfoToJson);

        return easypayService.downEasypay(EasypayInfoToJson);

    }

}
