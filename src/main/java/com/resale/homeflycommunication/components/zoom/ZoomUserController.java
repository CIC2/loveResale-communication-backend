package com.resale.homeflycommunication.components.zoom;

import com.resale.homeflycommunication.components.zoom.dto.CreateZoomUserDTO;
import com.resale.homeflycommunication.logging.LogActivity;
import com.resale.homeflycommunication.models.enums.ActionType;
import com.resale.homeflycommunication.utils.ReturnObject;
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

