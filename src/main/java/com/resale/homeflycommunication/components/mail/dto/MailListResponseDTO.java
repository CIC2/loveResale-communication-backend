package com.resale.homeflycommunication.components.mail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MailListResponseDTO {
    String customerName;
    String title;
    String message;
    String senderName;
    String customerMail;
    LocalDateTime sentAt;

}


