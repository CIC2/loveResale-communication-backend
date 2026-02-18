package com.resale.loveresalecommunication.models.enums;

public enum ActionType {
    UPLOAD_SMS_EXCEL(1),
    SEND_SINGLE_SMS(2),
    GET_ALL_SMS(3),
    CREATE_ZOOM_MEETING(4),
    SEND_ZOOM_LINK_TO_CUSTOMER (5),
    END_ZOOM_MEETING(6),
    GET_CUSTOMER_SIGNATURE(7),
    GET_ZOOM_RUNTIME_DATA(8),
    GET_AUTHORIZATION_URL(9),
    GET_ZOOM_CALLBACK(10),
    SEND_NOTIFICATION_TO_ALL(11),
    SEND_OTP_MAIL(12),
    SNED_MAIL(13),
    UPLOAD_EMAIL(14),
    GET_EMAILS(15),
    GET_NOTIFICATION_BY_CUSTOMER(16),
    MARK_CUSTOMER_NOTIFICATION_OPEN(17),
    CREATE_ZOOM_USER(18),

    UNKNOWN(99); // fallback

    private final int code;

    ActionType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    }


