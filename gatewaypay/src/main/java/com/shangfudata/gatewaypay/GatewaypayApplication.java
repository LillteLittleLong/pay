package com.shangfudata.gatewaypay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GatewaypayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewaypayApplication.class, args);
    }

}
