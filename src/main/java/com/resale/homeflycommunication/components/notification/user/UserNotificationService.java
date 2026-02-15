package com.resale.homeflycommunication.components.notification.user;

import com.resale.homeflycommunication.components.notification.user.dto.NotificationListResponseDTO;
import com.resale.homeflycommunication.components.notification.user.dto.NotificationResponseDTO;
import com.resale.homeflycommunication.models.Notification;
import com.resale.homeflycommunication.repositories.NotificationRepository;
import com.resale.homeflycommunication.utils.PaginatedResponseDTO;
import com.resale.homeflycommunication.utils.ReturnObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserNotificationService {

    @Autowired
    NotificationRepository notificationRepository;


    public ReturnObject<PaginatedResponseDTO<NotificationResponseDTO>> getNotificationsByUser(Integer userId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> notificationsPage = notificationRepository.findByUserIdAndSourceOrderBySentAtDesc(userId, 0, pageable);

        var dtoList = notificationsPage.getContent().stream()
                .map(n -> new NotificationResponseDTO(
                        n.getCustomerId(),
                        n.getContent(),
                        n.getType(),
                        n.getIsSeen(),
                        n.getIsOpened(),
                        n.getSentAt()
                ))
                .collect(Collectors.toList());

        PaginatedResponseDTO<NotificationResponseDTO> paginatedResponse = new PaginatedResponseDTO<>(
                dtoList,
                page,
                size,
                notificationsPage.getTotalElements(),
                notificationsPage.getTotalPages(),
                notificationsPage.isLast()
        );

        return new ReturnObject<>("Notifications retrieved successfully", true, paginatedResponse);
    }

    public ReturnObject<PaginatedResponseDTO<NotificationListResponseDTO>> findAllNotifications(String search, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<Notification> notificationsPage = notificationRepository.findBySourceOrderBySentAtDesc(0, pageable);

        var dtoList = notificationsPage.getContent().stream()
                .map(n -> new NotificationListResponseDTO(
                        String.valueOf(n.getCustomerId()),
                        n.getTitle(),
                        n.getContent(),
                        n.getUserId().toString(),
                        n.getSentAt()
                ))
                .collect(Collectors.toList());

        PaginatedResponseDTO<NotificationListResponseDTO> paginatedResponse = new PaginatedResponseDTO<>(
                dtoList,
                page,
                size,
                notificationsPage.getTotalElements(),
                notificationsPage.getTotalPages(),
                notificationsPage.isLast()
        );

        return new ReturnObject<>("Notifications retrieved successfully", true, paginatedResponse);
    }


    public ReturnObject<String> markNotificationAsOpened(Integer userId, Integer notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);

        if (notificationOpt.isEmpty()) {
            return new ReturnObject<>("Notification not found", false, null);
        }

        Notification notification = notificationOpt.get();

        if (!notification.getUserId().equals(userId) || notification.getSource() != 0) {
            return new ReturnObject<>("Permission denied or invalid notification source", false, null);
        }

        notification.setIsOpened(true);
        notificationRepository.save(notification);

        return new ReturnObject<>("Notification marked as opened", true, null);
    }
}

