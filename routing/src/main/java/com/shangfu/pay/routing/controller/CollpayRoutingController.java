package com.shangfu.pay.routing.controller;

import com.google.gson.Gson;
import com.shangfu.pay.routing.service.DownRoutingService;
import com.shangfu.pay.routing.service.UpRoutingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tinlly to 2019/4/2
 * Package for com.shangfu.pay.routing.controller
 */
@RestController
public class CollpayRoutingController {

    @Autowired
    DownRoutingService downRoutingService;
    @Autowired
    UpRoutingService upRoutingService;

    /**
     * collpay 下游路由接口
     */
    @PostMapping("/downRouting")
    @ResponseBody
    public ResponseEntity<String> collpayDownRouting(String downMchId, String downSpId, String totalFee , String passage) {
        Gson gson = new Gson();
        System.out.println("进入了 routing 下游接口");

        Map<String, String> map = new HashMap();
        map.put("down_mch_id", downMchId);
        map.put("down_sp_id", downSpId);
        map.put("total_fee", totalFee);
        map.put("passage" , passage);
        Map routingMap = new HashMap();
        Integer integer = downRoutingService.downRouting(map, routingMap);

        routingMap.put("down_busi_id", integer+"");

        return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(routingMap));
    }

    /**
     * collpay 上游路由接口
     */
    @PostMapping("/upRouting")
    @ResponseBody
    public ResponseEntity<String> collpayUpRouting(String downSpId, String mchId, String totalFee , String passage) {
        Gson gson = new Gson();
        System.out.println("进入了 routing 上游接口");

        Map<String, String> map = new HashMap();
        map.put("down_sp_id", downSpId);
        map.put("mch_id", mchId);
        map.put("total_fee", totalFee);
        map.put("passage", passage);

        Map routingMap = new HashMap();
        Integer integer = upRoutingService.upRouting(map, routingMap);

        routingMap.put("up_busi_id", integer+"");

        return ResponseEntity.status(HttpStatus.OK).body(gson.toJson(routingMap));
    }

}
