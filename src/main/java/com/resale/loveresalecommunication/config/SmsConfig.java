package com.resale.loveresalecommunication.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sms")
@Data
public class SmsConfig {
    private String accountid;
    private String apiPassword;
    private String senderName;
    private String secretKey;
    private String serverIp;
    private String serverAddress;
}


