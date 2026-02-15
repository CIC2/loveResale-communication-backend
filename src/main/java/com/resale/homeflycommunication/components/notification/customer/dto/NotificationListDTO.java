package com.resale.homeflycommunication.components.notification.customer.dto;

import com.resale.homeflycommunication.models.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationListDTO {
    private Integer id;
    private String content;
    private LocalDateTime sentAt;
    private Boolean isSeen;
    private Boolean isOpened;
    private NotificationType type;
    private Integer typeId;
}


