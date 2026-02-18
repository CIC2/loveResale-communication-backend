package com.resale.loveresalecommunication.components.notification.customer;

import com.resale.loveresalecommunication.components.notification.customer.dto.NotificationListDTO;
import com.resale.loveresalecommunication.logging.LogActivity;
import com.resale.loveresalecommunication.models.enums.ActionType;
import com.resale.loveresalecommunication.utils.PaginatedResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class CustomerNotificationController {

    private final CusomerNotificationService customerNotificationService;

    @GetMapping("/customer/{customerId}")
    @LogActivity(ActionType.GET_NOTIFICATION_BY_CUSTOMER)
    public ResponseEntity<PaginatedResponseDTO<NotificationListDTO>> getByCustomer(
            @PathVariable Integer customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                customerNotificationService.getNotificationsByCustomer(customerId, page, size)
        );
    }

    @PostMapping("/{id}/open")
    @LogActivity(ActionType.MARK_CUSTOMER_NOTIFICATION_OPEN)
    public boolean openNotification(
            @PathVariable("id") Integer notificationId,
            @RequestParam("customerId") Integer customerId
    ) {
        return customerNotificationService.markNotificationAsOpened(notificationId, customerId);
    }
}

