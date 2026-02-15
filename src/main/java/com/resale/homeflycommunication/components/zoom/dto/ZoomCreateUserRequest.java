package com.resale.homeflycommunication.components.zoom.dto;

import lombok.Data;

@Data
public class ZoomCreateUserRequest {
    private String action = "create";
    private ZoomUserInfo user_info = new ZoomUserInfo();
}

