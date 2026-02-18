package com.resale.loveresalecommunication.components.zoom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.resale.loveresalecommunication.components.firebase.FcmService;
import com.resale.loveresalecommunication.components.notification.customer.CusomerNotificationService;
import com.resale.loveresalecommunication.components.sms.SmsService;
import com.resale.loveresalecommunication.components.zoom.dto.*;
import com.resale.loveresalecommunication.feign.ZoomFeignClient;
import com.resale.loveresalecommunication.models.ZoomToken;
import com.resale.loveresalecommunication.models.enums.NotificationType;
import com.resale.loveresalecommunication.utils.ReturnObject;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoomMeetingService {

    private final ZoomService zoomService;
    private final ZoomFeignClient zoomFeignClient;
    private final SmsService smsService;
    private final FcmService fcmService;
    private final CusomerNotificationService notificationService;
    private final ObjectMapper objectMapper;

    public ResponseEntity<?> createMeeting(CreateZoomMeetingRequestDTO req) {
        log.info("📞 Creating Zoom meeting...");
        log.info("📝 Request payload → topic={}, start_time={}, duration={}",
                req.getTopic(), req.getStart_time(), req.getDuration());

        ZoomToken token;

        try {
            token = zoomService.getValidToken();
            log.info("🔐 Using access token: {}", token.getAccessToken());

        } catch (Exception ex) {
            log.error("❌ Token invalid or expired: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ReturnObject<>(
                            "Zoom authorization expired. Please login again.",
                            false,
                            null
                    ));
        }

        try {
            return executeCreateMeeting(token, req, false);

        } catch (FeignException.Unauthorized ex) {
            log.warn("⚠️ Initial call failed with 401. Access token may be revoked. Attempting token refresh and retry...");

            try {
                token = zoomService.refreshAccessToken(token.getRefreshToken());
                log.info("✅ Token successfully refreshed. Retrying meeting creation...");

                return executeCreateMeeting(token, req, true);

            } catch (Exception refreshEx) {
                log.error("❌ Token refresh failed after 401. User must re-authenticate: {}", refreshEx.getMessage(), refreshEx);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ReturnObject<>(
                                "Zoom authorization expired. Please login again.",
                                false,
                                null
                        ));
            }

        } catch (Exception ex) {
            log.error("❌ Error creating meeting: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReturnObject<>("Error: " + ex.getMessage(), false, null));
        }
    }

    private ResponseEntity<?> executeCreateMeeting(
            ZoomToken token,
            CreateZoomMeetingRequestDTO req,
            boolean isRetry) throws FeignException, Exception {

        String callType = isRetry ? "Retry call" : "Initial call";
        String hostId = req.getZoomHostId();
        log.info("➡️ {} Calling Zoom Create Meeting API...", callType);

        ResponseEntity<String> zoomResponse =
                zoomFeignClient.createMeeting(
                        "Bearer " + token.getAccessToken(),
                        hostId,
                        req
                );

        if (!zoomResponse.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(new ReturnObject<>("Failed to create Zoom meeting", false, null));
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode json = mapper.readTree(zoomResponse.getBody());

        String joinUrl = json.get("join_url").asText();
        String startUrl = json.get("start_url").asText();
        String meetingId = json.get("id").asText();
        String password = json.get("password").asText();

        // Extract once
        String zak = extractZak(startUrl);
        String signature = zoomService.generateZoomSignature(meetingId, 1); // host role

        ObjectNode result = mapper.createObjectNode();
        result.put("meetingId", meetingId);
        result.put("joinUrl", joinUrl);
        result.put("startUrl", startUrl);
        result.put("password", password);
        result.put("signature", signature);
        result.put("zak", zak);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ReturnObject<>("Meeting created", true, result));
    }


    public ResponseEntity<?> sendZoomLinkToCustomer(ZoomInfoDTO zoomInfoDTO) {

        String content = "Your Zoom meeting link: " + zoomInfoDTO.getZoomUrl();

        try {
            smsService.sendSms(zoomInfoDTO.getCustomerMobile(), zoomInfoDTO.getZoomUrl());
        } catch (Exception e) {
            System.out.println("SMS failed: " + e.getMessage());
        }

        boolean fcmSent = false;

        if (zoomInfoDTO.getCustomerFirebaseToken() != null &&
                !zoomInfoDTO.getCustomerFirebaseToken().isBlank()) {

            fcmSent = fcmService.sendToToken(
                    zoomInfoDTO.getCustomerFirebaseToken(),
                    content,
                    null,
                    NotificationType.ZOOM_LINK
            );
        }

        if (fcmSent) {
            notificationService.saveNotification(
                    zoomInfoDTO.getUserId(),
                    zoomInfoDTO.getCustomerId(),
                    NotificationType.ZOOM_LINK,
                    content,
                    1, // ADMIN
                    zoomInfoDTO.getZoomUrl(),
                    zoomInfoDTO.getAppointmentId()
            );
        } else {
            System.out.println("Notification NOT saved: FCM failed.");
        }

        return ResponseEntity.ok(zoomInfoDTO);
    }

    public String extractZak(String startUrl) {
        return startUrl.split("zak=")[1];
    }


    public ResponseEntity<?> endMeeting(String meetingId) {

        log.info(" Ending Zoom meeting with id={}", meetingId);

        ZoomToken token;

        try {
            token = zoomService.getValidToken();
            log.info(" Using access token");
        } catch (Exception ex) {
            log.error(" Token invalid or expired: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ReturnObject<>(
                            "Zoom authorization expired. Please login again.",
                            false,
                            null
                    ));
        }

        Map<String, String> body = new HashMap<>();
        body.put("action", "end");

        try {
            zoomFeignClient.endMeeting(
                    "Bearer " + token.getAccessToken(),
                    meetingId,
                    body
            );

            log.info("✅ Meeting ended successfully");

            return ResponseEntity.ok(
                    new ReturnObject<>("Meeting ended successfully", true, null)
            );

        } catch (FeignException.Unauthorized ex) {
            log.warn(" 401 Unauthorized. Trying token refresh...");

            try {
                token = zoomService.refreshAccessToken(token.getRefreshToken());

                zoomFeignClient.endMeeting(
                        "Bearer " + token.getAccessToken(),
                        meetingId,
                        body
                );

                log.info(" Meeting ended successfully after token refresh");

                return ResponseEntity.ok(
                        new ReturnObject<>("Meeting ended successfully", true, null)
                );

            } catch (Exception refreshEx) {
                log.error("Token refresh failed: {}", refreshEx.getMessage(), refreshEx);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ReturnObject<>(
                                "Zoom authorization expired. Please login again.",
                                false,
                                null
                        ));
            }

        } catch (FeignException.NotFound ex) {
            log.error(" Meeting not found or already ended");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ReturnObject<>("Meeting not found or already ended", false, null));

        } catch (Exception ex) {
            log.error(" Error ending meeting: {}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ReturnObject<>("Error: " + ex.getMessage(), false, null));
        }
    }

    public ReturnObject<ObjectNode> getZoomRuntimeData(String meetingId) {

        ZoomToken token;
        try {
            token = zoomService.getValidToken();
        } catch (Exception e) {
            return new ReturnObject<>(
                    "Zoom authorization expired",
                    false,
                    null
            );
        }

        ResponseEntity<String> zoomResponse =
                zoomFeignClient.getMeeting(
                        "Bearer " + token.getAccessToken(),
                        meetingId
                );

        if (!zoomResponse.getStatusCode().is2xxSuccessful()
                || zoomResponse.getBody() == null) {

            return new ReturnObject<>(
                    "Failed to fetch Zoom meeting data",
                    false,
                    null
            );
        }

        try {
            JsonNode json = objectMapper.readTree(zoomResponse.getBody());

            String startUrl = json.get("start_url").asText();
            String password = json.get("password").asText();
            String createdAt = json.hasNonNull("created_at")
                    ? json.get("created_at").asText()
                    : null;

            String zak = extractZak(startUrl);
            String signature = zoomService.generateZoomSignature(meetingId, 1);

            ObjectNode result = objectMapper.createObjectNode();
            result.put("meetingId", meetingId);
            result.put("startUrl", startUrl);
            result.put("password", password);
            result.put("zak", zak);
            result.put("signature", signature);
            result.put("createdAt", createdAt);

            return new ReturnObject<>(
                    "Zoom runtime data",
                    true,
                    result
            );

        } catch (Exception e) {
            log.error("Zoom runtime parse error", e);
            return new ReturnObject<>(
                    "Failed to parse Zoom response",
                    false,
                    null
            );
        }
    }


    public String getMeetingCreatedAt(String meetingId) {
        ZoomToken token = zoomService.getValidToken();
        ResponseEntity<String> response = zoomFeignClient.getMeeting(
                "Bearer " + token.getAccessToken(), meetingId
        );

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());
            return json.hasNonNull("created_at") ? json.get("created_at").asText() : null;
        } catch (Exception e) {
            log.error("Error parsing Zoom meeting created_at", e);
            return null;
        }
    }

}

