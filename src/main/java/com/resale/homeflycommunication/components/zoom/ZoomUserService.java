package com.resale.homeflycommunication.components.zoom;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resale.homeflycommunication.components.zoom.dto.CreateZoomUserDTO;
import com.resale.homeflycommunication.components.zoom.dto.ZoomCreateUserRequest;
import com.resale.homeflycommunication.feign.ZoomFeignClient;
import com.resale.homeflycommunication.models.ZoomToken;
import com.resale.homeflycommunication.utils.ReturnObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoomUserService {

    private final ZoomFeignClient zoomFeignClient;
    private final ZoomService zoomService;
    private String getAuthKey(String clientId, String clientSecret) {
        String originalInput = clientId + ":" + clientSecret;

        return Base64.getEncoder().encodeToString(originalInput.getBytes());
    }
    public ReturnObject<?> createZoomUser(CreateZoomUserDTO dto) {
        try {
            ZoomToken token = zoomService.getValidToken();

            if (!zoomService.testZoomToken(token.getAccessToken())) {
                log.warn("Token is invalid, refreshing...");
                token = zoomService.refreshAccessToken(token.getRefreshToken());
            }

            ZoomCreateUserRequest req = new ZoomCreateUserRequest();
            req.getUser_info().setEmail(dto.getEmail());
            req.getUser_info().setFirst_name(dto.getFirstName());
            req.getUser_info().setLast_name(dto.getLastName());
            req.getUser_info().setType(1);
            req.getUser_info().setAuthKey(getAuthKey("jimdSxz5TVO73Tfj8AASkQ","kp1p32jXiur65cLO5iDMgyDJGnRm72Rr"));

            ResponseEntity<String> response =
                    zoomFeignClient.createUser("Bearer " + token.getAccessToken(), req);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return new ReturnObject<>("Failed to create Zoom user", false, response.getBody());
            }

            JsonNode json = new ObjectMapper().readTree(response.getBody());
            String zoomUserId = json.get("id").asText();

            return new ReturnObject<>("Zoom user created", true, zoomUserId);

        } catch (Exception ex) {
            log.error("Error creating Zoom user: {}", ex.getMessage());
            return new ReturnObject<>(ex.getMessage(), false, null);
        }
    }
}

