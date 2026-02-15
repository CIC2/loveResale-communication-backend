package com.resale.homeflycommunication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients(basePackages = "com.tmg.tmgvsocommunication.feign")
public class TmgVsoCommunicationApplication {
    public static void main(String[] args) {
        SpringApplication.run(TmgVsoCommunicationApplication.class, args);
    }
}


