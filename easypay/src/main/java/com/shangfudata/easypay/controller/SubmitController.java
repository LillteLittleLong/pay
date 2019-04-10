package com.shangfudata.easypay.controller;

import com.shangfudata.easypay.service.SubmitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/easypay")
public class SubmitController {

    @Autowired
    SubmitService submitService;

    /**
     * 对下开放 提交  内部处理接口
     * @param EasypayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/submit", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Submit(@RequestBody String EasypayInfoToJson) throws Exception{

        return submitService.submit(EasypayInfoToJson);

    }

}
