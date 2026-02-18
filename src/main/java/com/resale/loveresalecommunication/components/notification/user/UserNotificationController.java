package com.resale.loveresalecommunication.components.notification.user;

import com.resale.loveresalecommunication.components.notification.user.dto.NotificationResponseDTO;
import com.resale.loveresalecommunication.security.JwtTokenUtil;
import com.resale.loveresalecommunication.utils.PaginatedResponseDTO;
import com.resale.loveresalecommunication.utils.ReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;
    private final JwtTokenUtil jwtTokenUtil;

    @GetMapping("/notification")
    public ResponseEntity<ReturnObject<PaginatedResponseDTO<NotificationResponseDTO>>> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof Jwt jwt)) {
            return new ResponseEntity<>(
                    new ReturnObject<>("Unauthorized", false, null),
                    HttpStatus.UNAUTHORIZED
            );
        }
        Integer userId = jwtTokenUtil.extractUserId(jwt.getTokenValue());

        ReturnObject<PaginatedResponseDTO<NotificationResponseDTO>> result = userNotificationService.getNotificationsByUser(userId, page, size);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }


    @PostMapping("/notification/open/{id}")
    public ResponseEntity<ReturnObject<String>> markNotificationAsOpened(@PathVariable("id") Integer notificationId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (!(principal instanceof org.springframework.security.oauth2.jwt.Jwt jwt)) {
            return new ResponseEntity<>(new ReturnObject<>("Unauthorized", false, null), HttpStatus.UNAUTHORIZED);
        }

        Integer userId = jwtTokenUtil.extractUserId(jwt.getTokenValue());

        ReturnObject<String> result = userNotificationService.markNotificationAsOpened(userId, notificationId);
        return new ResponseEntity<>(result, result.getStatus() ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }
    @GetMapping("/notification/all")
    public ResponseEntity<?> getAllNotifications(
            @RequestParam(value = "search",required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ReturnObject returnObject = userNotificationService.findAllNotifications(search, page, size);
        if(returnObject.getStatus()) {
            return ResponseEntity.ok(
                    returnObject
            );
        }else{
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(returnObject);
        }
        return ResponseEntity.ok(
                returnObject
        );
    }

}

