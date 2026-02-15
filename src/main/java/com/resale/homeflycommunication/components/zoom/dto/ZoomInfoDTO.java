package com.resale.homeflycommunication.components.zoom.dto;

import lombok.Data;

@Data
public class ZoomInfoDTO {
    Integer customerId;
    Integer userId;
    String customerName;
    String customerMobile;
    String customerMail;
    String customerFirebaseToken;
    String zoomUrl;
    private Integer appointmentId;
}


