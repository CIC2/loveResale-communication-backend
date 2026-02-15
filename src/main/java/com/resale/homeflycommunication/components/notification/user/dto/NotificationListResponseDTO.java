package com.resale.homeflycommunication.components.notification.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationListResponseDTO {
    String customerName;
    String title;
    String message;
    String senderName;
    LocalDateTime sentAt;

}

