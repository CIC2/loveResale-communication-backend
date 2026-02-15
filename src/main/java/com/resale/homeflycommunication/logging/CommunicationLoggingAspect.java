package com.resale.homeflycommunication.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resale.homeflycommunication.models.enums.ActionType;
import com.resale.homeflycommunication.security.CookieBearerTokenResolver;
import com.resale.homeflycommunication.security.JwtTokenUtil;
import com.resale.homeflycommunication.utils.RequestBodyCachingFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import com.resale.homeflycommunication.models.CommunicationLog;
import com.resale.homeflycommunication.models.CommunicationExceptionLog;
import com.resale.homeflycommunication.repositories.CommunicationLogRepository;
import com.resale.homeflycommunication.repositories.CommunicationExceptionLogRepository;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CommunicationLoggingAspect {

    private final CommunicationLogRepository communicationLogRepository;
    private final CommunicationExceptionLogRepository communicationExceptionLogRepository;
    private final HttpServletRequest request;
    private final JwtTokenUtil jwtTokenUtil;
    private final CookieBearerTokenResolver cookieBearerTokenResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(logActivity)")
    public Object log(ProceedingJoinPoint joinPoint, LogActivity logActivity) throws Throwable {

        ActionType actionType = logActivity.value();
        CommunicationLog communicationLog = new CommunicationLog();
        CommunicationExceptionLog communicationExceptionLog = new CommunicationExceptionLog();
        long start = System.currentTimeMillis();
        String httpMethod = request.getMethod();

        String identityType = "GUEST";
        Integer identityId = null;
        String token = null;

        try {
            token = cookieBearerTokenResolver.resolve(request);
        } catch (Exception ignored) {
        }

        if (token != null) {
            try {
                Integer customerId = jwtTokenUtil.extractCustomerId(token);
                if (customerId != null) {
                    identityType = "CUSTOMER";
                    identityId = customerId;
                }
            } catch (Exception ignored) {
            }

            if (identityId == null) {
                try {
                    Integer userId = jwtTokenUtil.extractUserId(token);
                    if (userId != null) {
                        identityType = "USER";
                        identityId = userId;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (identityId == null) {
            try {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
                    Integer customerId = jwt.getClaim("customerId");
                    Integer userId = jwt.getClaim("userId");

                    if (customerId != null) {
                        identityType = "CUSTOMER";
                        identityId = customerId;
                    } else if (userId != null) {
                        identityType = "USER";
                        identityId = userId;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        int actionCode = actionType.getCode();
        String actionName = actionType.name();

        String requestBodyJson = null;
        if (!"GET".equalsIgnoreCase(httpMethod)) {
            requestBodyJson = extractRequestBody();
        }

        String headersJson = extractHeaders();
        String paramsJson = extractQueryParams();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            String responseJson = null;
            try {
                responseJson = objectMapper.writeValueAsString(result);
            } catch (Exception ignored) {
            }

            int status = 200;
            if (result instanceof ResponseEntity<?> res) {
                status = res.getStatusCode().value();
            }

            communicationLog.setIdentityType(identityType);
            communicationLog.setIdentityId(identityId);
            communicationLog.setActionCode(actionCode);
            communicationLog.setActionName(actionName);
            communicationLog.setHttpMethod(httpMethod);
            communicationLog.setStatusCode(status);
            communicationLog.setRequestBody(requestBodyJson);
            communicationLog.setResponseBody(responseJson);
            communicationLog.setExecutionTimeMs(executionTime);
            communicationLog.setCreatedAt(LocalDateTime.now());
            communicationLog.setHeaders(headersJson);
            communicationLog.setQueryParams(paramsJson);

            communicationLogRepository.save(communicationLog);

            return result;

        } catch (Exception ex) {

            communicationExceptionLog.setIdentityType(identityType);
            communicationExceptionLog.setIdentityId(identityId);
            communicationExceptionLog.setActionCode(actionCode);
            communicationExceptionLog.setActionName(actionName);
            communicationExceptionLog.setHttpMethod(httpMethod);
            communicationExceptionLog.setExceptionType(ex.getClass().getSimpleName());
            communicationExceptionLog.setMessage(ex.getMessage());
            communicationExceptionLog.setStacktrace(getStackTrace(ex));
            communicationExceptionLog.setCreatedAt(LocalDateTime.now());
            communicationExceptionLog.setHeaders(headersJson);
            communicationExceptionLog.setQueryParams(paramsJson);

            communicationExceptionLogRepository.save(communicationExceptionLog);

            throw ex;
        }
    }

    private String extractHeaders() {
        try {
            Map<String, String> headers = new LinkedHashMap<>();
            Enumeration<String> names = request.getHeaderNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                headers.put(name, request.getHeader(name));
            }
            return objectMapper.writeValueAsString(headers);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractQueryParams() {
        try {
            return objectMapper.writeValueAsString(request.getParameterMap());
        } catch (Exception e) {
            return null;
        }
    }

    private String getStackTrace(Throwable t) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement ste : t.getStackTrace()) {
            sb.append(ste).append("\n");
        }
        return sb.toString();
    }


    private String extractRequestBody() {
        try {
            Object cached = request.getAttribute(RequestBodyCachingFilter.CACHED_REQUEST);

            if (!(cached instanceof ContentCachingRequestWrapper wrapper)) {
                return null;
            }

            byte[] content = wrapper.getContentAsByteArray();
            if (content.length == 0) {
                return null;
            }

            return new String(content, StandardCharsets.UTF_8);

        } catch (Exception e) {
            return null;
        }
    }
}


