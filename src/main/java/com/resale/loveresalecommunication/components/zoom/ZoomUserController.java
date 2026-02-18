package com.resale.loveresalecommunication.components.zoom;

import com.resale.loveresalecommunication.components.zoom.dto.CreateZoomUserDTO;
import com.resale.loveresalecommunication.logging.LogActivity;
import com.resale.loveresalecommunication.models.enums.ActionType;
import com.resale.loveresalecommunication.utils.ReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class ZoomUserController {

    @Autowired
    ZoomUserService zoomUserService;

    @PostMapping("/zoom/createUser")
    @LogActivity(ActionType.CREATE_ZOOM_USER)
    public ResponseEntity<?> createZoomUser(@RequestBody CreateZoomUserDTO dto) {
        ReturnObject<?> res = zoomUserService.createZoomUser(dto);

        return ResponseEntity
                .status(res.getStatus() ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST)
                .body(res);
    }
}

