package com.resale.loveresalecommunication.components.zoom.dto;

import lombok.Data;

@Data
public class ZoomUserInfo {
    private String id;
    private String email;
    private Integer type = 1;
    private String authKey ;
    private String first_name;
    private String last_name;
}

