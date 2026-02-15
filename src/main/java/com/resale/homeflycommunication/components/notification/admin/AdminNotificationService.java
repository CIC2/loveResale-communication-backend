package com.resale.homeflycommunication.components.notification.admin;

import com.resale.homeflycommunication.components.firebase.FcmService;
import com.resale.homeflycommunication.components.notification.admin.dto.AdminNotificationDTO;
import com.resale.homeflycommunication.components.notification.admin.dto.CustomerFcmToken;
import com.resale.homeflycommunication.feign.CustomerClient;
import com.resale.homeflycommunication.models.Notification;
import com.resale.homeflycommunication.models.enums.NotificationType;
import com.resale.homeflycommunication.repositories.NotificationRepository;
import com.resale.homeflycommunication.security.JwtTokenUtil;
import com.resale.homeflycommunication.utils.ReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final CustomerClient customerClient;
    private final FcmService fcmService;
    private final NotificationRepository notificationRepository;
    private final JwtTokenUtil jwtTokenUtil;

    public ReturnObject<String> sendNotificationToAllCustomers(AdminNotificationDTO dto, String token) {
        Integer adminId = jwtTokenUtil.extractUserId(token);
        if (adminId == null) {
            return new ReturnObject<>("Invalid admin token", false, null);
        }

        ReturnObject<List<CustomerFcmToken>> response = customerClient.getAllFcmTokensWithIds();
        if (!response.getStatus() || response.getData() == null || response.getData().isEmpty()) {
            return new ReturnObject<>("No FCM tokens available", false, null);
        }

        List<CustomerFcmToken> customers = response.getData();

        customers.forEach(c -> fcmService.sendToToken(c.getFcmToken(), dto.getContent(), null, NotificationType.ADMIN_NOTIFICATION));

        List<Notification> notifications = customers.stream().map(c -> {
            Notification n = new Notification();
            n.setContent(dto.getContent());
            n.setTitle(dto.getTitle());
            n.setType(NotificationType.ADMIN_NOTIFICATION);
            n.setSentAt(LocalDateTime.now());
            n.setSource(1);
            n.setCustomerId(c.getCustomerId());
            n.setUserId(adminId);
            return n;
        }).collect(Collectors.toList());

        notificationRepository.saveAll(notifications);

        return new ReturnObject<>("Notification sent to all customers", true, null);
    }
}


