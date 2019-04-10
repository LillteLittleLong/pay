package com.shangfudata.gatewaypay.controller;

import com.shangfudata.gatewaypay.service.GatewaypayService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gatewaypay")
public class GatewaypayController {

    @Autowired
    GatewaypayService gatewaypayService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 对下开放请求内部处理接口
     * @param gatewaypayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/trading", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Gatewaypay(@RequestBody String gatewaypayInfoToJson) throws Exception{

        logger.info("接口接收下游交易请求信息："+gatewaypayInfoToJson);

        return gatewaypayService.downGatewaypay(gatewaypayInfoToJson);
    }
}
