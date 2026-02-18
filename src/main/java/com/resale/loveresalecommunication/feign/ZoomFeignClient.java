package com.resale.loveresalecommunication.feign;

import com.resale.loveresalecommunication.components.zoom.dto.CreateZoomMeetingRequestDTO;
import com.resale.loveresalecommunication.components.zoom.dto.ZoomCreateUserRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "zoomClient", url = "${zoom.api.base-url}")
public interface ZoomFeignClient {


    @PostMapping("/users/{hostId}/meetings")
    ResponseEntity<String> createMeeting(
            @RequestHeader("Authorization") String token,
            @PathVariable("hostId") String hostId,
            @RequestBody CreateZoomMeetingRequestDTO request
    );

    @PostMapping("/users")
    ResponseEntity<String> createUser(
            @RequestHeader("Authorization") String token,
            @RequestBody ZoomCreateUserRequest request
    );

    @PutMapping("/meetings/{meetingId}/status")
    ResponseEntity<Void> endMeeting(
            @RequestHeader("Authorization") String token,
            @PathVariable("meetingId") String meetingId,
            @RequestBody Map<String, String> body
    );

    @GetMapping("/meetings/{meetingId}")
    ResponseEntity<String> getMeeting(
            @RequestHeader("Authorization") String token,
            @PathVariable("meetingId") String meetingId
    );
}



