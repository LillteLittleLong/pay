package com.shangfudata.collpay.controller;

import com.shangfudata.collpay.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/collpay")
public class QueryController {

    @Autowired
    QueryService queryService;

    Logger logger = LoggerFactory.getLogger(this.getClass());



    @RequestMapping(value = "/query",method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Query(@RequestBody String CollpayInfoToJson){
        logger.info("下游查询参数："+CollpayInfoToJson);
        return queryService.downQuery(CollpayInfoToJson);

    }

}
