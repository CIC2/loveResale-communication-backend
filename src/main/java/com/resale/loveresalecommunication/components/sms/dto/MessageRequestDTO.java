package com.resale.loveresalecommunication.components.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDTO {
    private String title;
    private String messageContent;
    private boolean sendToAll;

}

