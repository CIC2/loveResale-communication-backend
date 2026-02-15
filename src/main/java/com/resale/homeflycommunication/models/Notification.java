package com.resale.homeflycommunication.models;

import com.resale.homeflycommunication.models.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer userId;
    private Integer customerId;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    private Integer source;

    private Boolean isSeen = false;
    private Boolean isOpened = false;

    @Enumerated(EnumType.STRING)
    private NotificationType type;

    private Integer appointmentId;
    private String title;


    @Column(columnDefinition = "LONGTEXT")
    private String zoomUrl;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

}


