package com.shangfudata.collpay.controller;

import com.shangfudata.collpay.service.impl.CollpayServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collpay")
public class CollpayController {

    @Autowired
    CollpayServiceImpl collpayService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 对下开放请求内部处理接口
     * @param CollpayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/trading", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Collpay(@RequestBody String CollpayInfoToJson) throws Exception{
        logger.info("接口接收下游交易请求信息："+CollpayInfoToJson);
        return collpayService.downCollpay(CollpayInfoToJson);

    }




}
