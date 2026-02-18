package com.resale.loveresalecommunication.components.sms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleSMSRequestDTO {
    String mobile;
    String content;
}


