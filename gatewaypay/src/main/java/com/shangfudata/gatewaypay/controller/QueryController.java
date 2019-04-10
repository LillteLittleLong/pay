package com.shangfudata.gatewaypay.controller;

import com.shangfudata.gatewaypay.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gatewaypay")
public class QueryController {

    @Autowired
    QueryService queryService;


    @PostMapping(value = "/query", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Query(@RequestBody String gatewaypayInfoToJson){
        System.out.println("传进来的查询参数"+gatewaypayInfoToJson);
        return queryService.downQuery(gatewaypayInfoToJson);
    }

}
