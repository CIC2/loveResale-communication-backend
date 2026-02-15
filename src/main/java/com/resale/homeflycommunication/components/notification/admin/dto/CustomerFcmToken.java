package com.resale.homeflycommunication.components.notification.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFcmToken {
    private Integer customerId;
    private String fcmToken;
}


