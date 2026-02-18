package com.resale.loveresalecommunication.components.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.resale.loveresalecommunication.models.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;

    public boolean sendToTopic(String topicName, String content, Long notificationTypeId, NotificationType type) {
        try {
            Message message = Message.builder()
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle("New Notification")
                            .setBody(content)
                            .build())
                    .putData("title", "New Notification")
                    .putData("body", content)
                    .putData("type", type != null ? type.name() : "")
                    .putData("id", notificationTypeId != null ? String.valueOf(notificationTypeId) : "")
                    .setTopic(topicName)
                    .build();

            String response = firebaseMessaging.send(message);
            System.out.println("FCM Topic Message ID: " + response);

            return true;
        } catch (FirebaseMessagingException e) {
            System.err.println("Error sending topic message: " + e.getMessage());
            return false;
        }
    }


    public boolean sendToToken(String token, String content, Long notificationTypeId, NotificationType type) {
        try {
            Message message = Message.builder()
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle("New Notification")
                            .setBody(content)
                            .build())
                    .putData("title", "New Notification")
                    .putData("body", content)
                    .putData("type", type != null ? type.name() : "")
                    .putData("id", notificationTypeId != null ? String.valueOf(notificationTypeId) : "")
                    .setToken(token)
                    .build();

            String response = firebaseMessaging.send(message);
            System.out.println("FCM Message ID: " + response);

            return true;

        } catch (Exception e) {
            System.err.println("FCM error: " + e.getMessage());
            return false;
        }
    }
}

