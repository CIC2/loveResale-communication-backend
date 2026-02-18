package com.resale.loveresalecommunication.components.notification.admin;

import com.resale.loveresalecommunication.components.notification.admin.dto.AdminNotificationDTO;
import com.resale.loveresalecommunication.logging.LogActivity;
import com.resale.loveresalecommunication.models.enums.ActionType;
import com.resale.loveresalecommunication.security.CheckPermission;
import com.resale.loveresalecommunication.security.CookieBearerTokenResolver;
import com.resale.loveresalecommunication.utils.ReturnObject;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;
    private final CookieBearerTokenResolver tokenResolver;

    @PostMapping("/sendToAll")
    @CheckPermission(value = {"admin:login"})
    @LogActivity(ActionType.SEND_NOTIFICATION_TO_ALL)
    public ResponseEntity<ReturnObject<?>> sendToAll(@RequestBody AdminNotificationDTO dto,
                                                     HttpServletRequest request) {
        String token = tokenResolver.resolve(request);
        ReturnObject<?> result = adminNotificationService.sendNotificationToAllCustomers(dto, token);
        return new ResponseEntity<>(result, result.getStatus() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
}


