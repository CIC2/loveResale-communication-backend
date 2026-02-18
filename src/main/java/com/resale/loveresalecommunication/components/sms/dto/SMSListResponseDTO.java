package com.resale.loveresalecommunication.components.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SMSListResponseDTO {
    String customerName;
    String title;
    String message;
    String senderName;
    String customerMobile;
    LocalDateTime sentAt;

}


