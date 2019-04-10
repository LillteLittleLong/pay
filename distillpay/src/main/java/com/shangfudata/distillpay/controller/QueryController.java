package com.shangfudata.distillpay.controller;

import com.shangfudata.distillpay.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/distillpay")
public class QueryController {

    @Autowired
    QueryService queryService;


    @RequestMapping(value = "/query",method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Query(@RequestBody String distillpayInfoToJson){
        System.out.println("传进来的查询参数"+distillpayInfoToJson);
        return queryService.downQuery(distillpayInfoToJson);

    }

}
