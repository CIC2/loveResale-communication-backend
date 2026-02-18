package com.resale.loveresalecommunication.components.notification.user.dto;

import com.resale.loveresalecommunication.models.enums.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDTO {
    private Integer customerId;
    private String content;
    private NotificationType type;
    private Boolean isSeen;
    private Boolean isOpened;
    private LocalDateTime sentAt;
}


