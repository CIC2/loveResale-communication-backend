package com.resale.homeflycommunication.components.notification.customer;

import com.resale.homeflycommunication.components.notification.customer.dto.NotificationListDTO;
import com.resale.homeflycommunication.models.Notification;
import com.resale.homeflycommunication.models.enums.NotificationType;
import com.resale.homeflycommunication.repositories.NotificationRepository;
import com.resale.homeflycommunication.utils.PaginatedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CusomerNotificationService {

    private final NotificationRepository notificationRepository;


    public PaginatedResponseDTO<NotificationListDTO> getNotificationsByCustomer(
            Integer customerId, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> pageResult = notificationRepository.findByCustomerIdAndSourceOrderBySentAtDesc(
                customerId, 1, pageable);

        // Mark all as seen
        List<Notification> notifications = pageResult.getContent();
        notifications.forEach(n -> n.setIsSeen(true));
        notificationRepository.saveAll(notifications);

        List<NotificationListDTO> dtos = notifications.stream()
                .map(n -> new NotificationListDTO(
                        n.getId(),
                        n.getContent(),
                        n.getSentAt(),
                        n.getIsSeen(),
                        n.getIsOpened(),
                        n.getType(),
                        n.getAppointmentId()
                ))
                .toList();

        return new PaginatedResponseDTO<>(
                dtos,
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.isLast()
        );
    }


    public void saveNotification(Integer userId,
                                 Integer customerId,
                                 NotificationType type,
                                 String content,
                                 Integer source,
                                 String zoomUrl,
                                 Integer appointmentId) {

        Notification n = new Notification();
        n.setUserId(userId);
        n.setCustomerId(customerId);
        n.setType(type);
        n.setContent(content);
        n.setZoomUrl(zoomUrl);
        n.setSentAt(LocalDateTime.now());
        n.setSource(source);
        n.setIsSeen(false);
        n.setIsOpened(false);
        n.setAppointmentId(appointmentId);

        notificationRepository.save(n);
    }

    public boolean markNotificationAsOpened(Integer notificationId, Integer customerId) {
        final int ADMIN_SOURCE = 1;

        Optional<Notification> notificationOpt =
                notificationRepository.findByIdAndCustomerIdAndSource(notificationId, customerId, ADMIN_SOURCE);

        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setIsOpened(true);
            notificationRepository.save(notification);
            return true;
        } else {
            return false;
        }
    }
}



