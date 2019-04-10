package com.shangfudata.distillpay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DistillpayApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistillpayApplication.class, args);
    }

}
