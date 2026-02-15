package com.resale.homeflycommunication.models;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Entity
@Table(name = "zoom_token")
@Data
public class ZoomToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private Timestamp createdAt;
}


