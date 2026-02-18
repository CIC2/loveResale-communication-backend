package com.resale.loveresalecommunication.components.zoom;

import com.resale.loveresalecommunication.logging.LogActivity;
import com.resale.loveresalecommunication.models.enums.ActionType;
import com.resale.loveresalecommunication.utils.ReturnObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zoom/auth")
public class ZoomOAuthController {

    @Autowired
     ZoomService zoomService;

    @GetMapping("/authorizeUrl")
    @LogActivity(ActionType.GET_AUTHORIZATION_URL)
    public ResponseEntity<?> getAuthorizationUrl() {
        String url = zoomService.generateAuthorizationUrl();
        return ResponseEntity.ok(url);
    }


    @GetMapping("/callback")
    @LogActivity(ActionType.GET_ZOOM_CALLBACK)
    public ResponseEntity<?> zoomCallback(@RequestParam String code) {

        ReturnObject<String> result = zoomService.exchangeCodeForToken(code);

        if (result.getStatus()) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(result);
        }
    }
}

