package com.resale.homeflycommunication.components.zoom;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.resale.homeflycommunication.feign.ZoomFeignClient;
import com.resale.homeflycommunication.models.ZoomToken;
import com.resale.homeflycommunication.repositories.ZoomTokenRepository;
import com.resale.homeflycommunication.utils.ReturnObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.sql.Timestamp;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoomService {

    private final ZoomTokenRepository zoomTokenRepository;
    private final ZoomFeignClient zoomFeignClient;

    @Value("${zoom.client.id}")
    private String clientId;

    @Value("${zoom.client.secret}")
    private String clientSecret;

    @Value("${zoom.redirect.uri}")
    private String redirectUri;


    @Value("${zoom.client.secret}")
    private String ZOOM_SDK_SECRET;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateAuthorizationUrl() {
        return "https://zoom.us/oauth/authorize"
                + "?response_type=code"
                + "&client_id=" + clientId
                + "&redirect_uri=" + redirectUri;
    }


    public ReturnObject<String> exchangeCodeForToken(String authorizationCode) {
        try {
            String url = "https://zoom.us/oauth/token"
                    + "?grant_type=authorization_code"
                    + "&code=" + authorizationCode
                    + "&redirect_uri=" + redirectUri;

            String credentials = clientId + ":" + clientSecret;
            String base64Creds = Base64.getEncoder().encodeToString(credentials.getBytes());

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + base64Creds);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                return new ReturnObject<>("Failed to get access token from Zoom", false, null);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());

            String accessToken = json.get("access_token").asText();
            String refreshToken = json.get("refresh_token").asText();
            Long expiresIn = json.get("expires_in").asLong();

            ZoomToken token = new ZoomToken();
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresIn(expiresIn);
            token.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            zoomTokenRepository.save(token);

            return new ReturnObject<>("Zoom authentication successful → tokens saved", true, null);

        } catch (Exception e) {
            e.printStackTrace();
            return new ReturnObject<>("Error occurred: " + e.getMessage(), false, null);
        }
    }


    public ZoomToken getValidToken() {
        log.info("🔍 Fetching latest Zoom token from database...");

        ZoomToken token = zoomTokenRepository.findTopByOrderByIdDesc();

        if (token == null) {
            log.error("❌ No Zoom token found in DB.");
            throw new RuntimeException("No Zoom token found. Please authenticate first.");
        }

        long now = System.currentTimeMillis();
        long tokenExpiry = token.getCreatedAt().getTime() + (token.getExpiresIn() * 1000);

        log.info("🕒 Token createdAt={} expiresIn={} seconds", token.getCreatedAt(), token.getExpiresIn());
        log.info("🕒 Token expires at: {}", new Timestamp(tokenExpiry));

        if (now >= tokenExpiry - 60000) {
            log.warn("⚠️ Token is expired or about to expire → refreshing...");
            token = refreshAccessToken(token.getRefreshToken());
            log.info("✅ Token refreshed successfully.");
        } else {
            log.info("✅ Token still valid → no refresh needed.");
        }

        return token;
    }


    public ZoomToken refreshAccessToken(String refreshToken) {
        try {
            log.info("🔄 Calling Zoom API to refresh access token...");

            String url = "https://zoom.us/oauth/token"
                    + "?grant_type=refresh_token"
                    + "&refresh_token=" + refreshToken;

            log.debug("🔗 Refresh URL: {}", url);

            String credentials = clientId + ":" + clientSecret;
            String base64Creds = Base64.getEncoder().encodeToString(credentials.getBytes());
            log.debug("🔐 Sending basic auth with clientId={}", clientId);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Basic " + base64Creds);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            ResponseEntity<String> response =
                    restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), String.class);

            log.info("📩 Zoom Response Status: {}", response.getStatusCode());
            log.debug("📄 Zoom Response Body: {}", response.getBody());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("❌ Failed to refresh token. Response={}", response.getBody());
                throw new RuntimeException("Failed to refresh Zoom token");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());

            ZoomToken newToken = new ZoomToken();
            newToken.setAccessToken(json.get("access_token").asText());
            newToken.setRefreshToken(json.get("refresh_token").asText());
            newToken.setExpiresIn(json.get("expires_in").asLong());
            newToken.setCreatedAt(new Timestamp(System.currentTimeMillis()));

            zoomTokenRepository.save(newToken);

            log.info("✅ New token saved. Expires in {} seconds.", newToken.getExpiresIn());

            return newToken;

        } catch (Exception ex) {
            log.error("❌ Error refreshing Zoom token: {}", ex.getMessage(), ex);
            throw new RuntimeException("Error refreshing Zoom token: " + ex.getMessage(), ex);
        }
    }

    public boolean testZoomToken(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.zoom.us/v2/users/me",
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            log.info("Zoom token test response: {}", response.getStatusCode());
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException ex) {
            log.error("Zoom token test failed: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return false;
        } catch (Exception ex) {
            log.error("Zoom token test error: {}", ex.getMessage(), ex);
            return false;
        }
    }

    public String generateZoomSignature(String meetingNumber, int role) {
        try {
            long iat = (System.currentTimeMillis() / 1000L) - 30;
            long exp = iat + 60 * 60 * 2;

            Algorithm algorithm = Algorithm.HMAC256(ZOOM_SDK_SECRET);

            return JWT.create()
                    .withClaim("appKey", clientId)
                    .withClaim("sdkKey", clientId)
                    .withClaim("mn", meetingNumber)
                    .withClaim("role", role)
                    .withClaim("iat", iat)
                    .withClaim("exp", exp)
                    .withClaim("tokenExp", exp)
                    .sign(algorithm);


        } catch (Exception e) {
            throw new RuntimeException("Error generating Zoom signature", e);
        }
    }

    public String getMeetingPassword(String meetingId) {
        ZoomToken token = getValidToken();
        ResponseEntity<String> response = zoomFeignClient.getMeeting("Bearer " + token.getAccessToken(), meetingId);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());
            return json.has("password") ? json.get("password").asText() : "";
        } catch (Exception e) {
            return "";
        }
    }
}

