package com.shangfudata.distillpay.eureka;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by tinlly to 2019/4/2
 * Package for com.shangfudata.collpay.eureka
 */
@Service
@FeignClient("routing-client")
public interface EurekaDistillpayClient {

    /**
     * 调用 routing 下游路由分发接口
     */
    @PostMapping("/downRouting")
    String downRouting(@RequestParam String downMchId, @RequestParam String downSpId, @RequestParam String totalFee , @RequestParam String passage);

    /**
     * 调用 routing 上游路由分发接口
     */
    @PostMapping("/upRouting")
    String upRouting(@RequestParam String mchId, @RequestParam String spId, @RequestParam String totalFee , @RequestParam String passage);

}