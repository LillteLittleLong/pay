package com.shangfudata.easypay.controller;

import com.shangfudata.easypay.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/easypay")
public class QueryController {

    @Autowired
    QueryService queryService;


    @RequestMapping(value = "/query",method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Query(@RequestBody String CollpayInfoToJson){
        System.out.println("传进来的查询参数json串：：："+CollpayInfoToJson);
        return queryService.downQuery(CollpayInfoToJson);
    }

}
