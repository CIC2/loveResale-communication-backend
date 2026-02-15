package com.resale.homeflycommunication.components.zoom;

import com.resale.homeflycommunication.components.zoom.dto.CreateZoomMeetingRequestDTO;
import com.resale.homeflycommunication.components.zoom.dto.ZoomInfoDTO;
import com.resale.homeflycommunication.logging.LogActivity;
import com.resale.homeflycommunication.models.enums.ActionType;
import com.resale.homeflycommunication.utils.ReturnObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/zoom")
@RequiredArgsConstructor
public class ZoomMeetingController {

    private final ZoomMeetingService zoomMeetingService;
    private final ZoomService zoomService;

    @PostMapping("/createMeeting")
    @LogActivity(ActionType.CREATE_ZOOM_MEETING)
    public ResponseEntity<?> createZoomMeeting(@RequestBody CreateZoomMeetingRequestDTO req) {
        return zoomMeetingService.createMeeting(req);
    }

    @PostMapping("sendZoomLink")
    @LogActivity(ActionType.SEND_ZOOM_LINK_TO_CUSTOMER)
    public ResponseEntity<?> sendZoomLinkToCustomer(@RequestBody ZoomInfoDTO zoomInfoDTO) {
        return zoomMeetingService.sendZoomLinkToCustomer(zoomInfoDTO);
    }

    @PutMapping("/endMeeting/{meetingId}")
    @LogActivity(ActionType.END_ZOOM_MEETING)
    public ResponseEntity<?> endZoomMeeting(@PathVariable String meetingId) {
        return zoomMeetingService.endMeeting(meetingId);
    }

    @GetMapping("/signature/customer/{meetingId}")
    @LogActivity(ActionType.GET_CUSTOMER_SIGNATURE)
    public ResponseEntity<ReturnObject<Map<String, String>>> getCustomerSignature(
            @PathVariable String meetingId
    ) {
        String signature = zoomService.generateZoomSignature(meetingId, 0);
        String password = zoomService.getMeetingPassword(meetingId);
        String createdAt = zoomMeetingService.getMeetingCreatedAt(meetingId);

        Map<String, String> data = new HashMap<>();
        data.put("signature", signature);
        data.put("password", password);
        data.put("createdAt", createdAt);

        return ResponseEntity.ok(
                new ReturnObject<>("Success", true, data)
        );
    }

    @GetMapping("/meeting/{meetingId}")
    @LogActivity(ActionType.GET_ZOOM_RUNTIME_DATA)
    public ResponseEntity<ReturnObject<?>> getZoomRuntimeData(
            @PathVariable String meetingId
    ) {
        ReturnObject<?> result =
                zoomMeetingService.getZoomRuntimeData(meetingId);

        return ResponseEntity
                .status(result.getStatus() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(result);
    }
}

