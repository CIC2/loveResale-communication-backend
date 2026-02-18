package com.resale.loveresalecommunication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication
@EnableFeignClients(basePackages = "com.tmg.tmgvsocommunication.feign")
public class LoveResaleCommunicationApplication {
    public static void main(String[] args) {
        SpringApplication.run(LoveResaleCommunicationApplication.class, args);
    }
}


