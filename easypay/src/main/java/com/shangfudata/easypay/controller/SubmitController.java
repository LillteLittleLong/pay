package com.shangfudata.easypay.controller;

import com.shangfudata.easypay.service.SubmitService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/easypay")
public class SubmitController {

    @Autowired
    SubmitService submitService;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 对下开放 提交  内部处理接口
     * @param EasypayInfoToJson
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/submit", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String Submit(@RequestBody String EasypayInfoToJson) {

        logger.info("接口接收下游提交请求信息："+EasypayInfoToJson);

        return submitService.submit(EasypayInfoToJson);

    }

}
