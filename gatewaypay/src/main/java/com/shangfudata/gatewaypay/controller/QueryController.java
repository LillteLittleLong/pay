package com.shangfudata.gatewaypay.controller;

import com.shangfudata.gatewaypay.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gatewaypay")
public class QueryController {

    @Autowired
    QueryService queryService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @PostMapping(value = "/query", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Query(@RequestBody String gatewaypayInfoToJson){
        logger.info("下游查询参数："+gatewaypayInfoToJson);
        return queryService.downQuery(gatewaypayInfoToJson);
    }

}
