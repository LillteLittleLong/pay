package com.shangfudata.distillpay.controller;

import com.shangfudata.distillpay.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distillpay")
public class QueryController {

    @Autowired
    QueryService queryService;

    Logger logger = LoggerFactory.getLogger(this.getClass());


    @RequestMapping(value = "/query",method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Query(@RequestBody String distillpayInfoToJson){
        logger.info("下游查询参数："+distillpayInfoToJson);
        return queryService.downQuery(distillpayInfoToJson);

    }

}
