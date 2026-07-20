package com.cinebook.common.security;


import com.cinebook.common.exception.ApiErrorResponse;
import com.cinebook.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Triggered when an unauthenticated request hits a protected endpoint
 * (missing/invalid/expired access token) -> 401 with the standard error body.
 */
@Component
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        ErrorCode ec = ErrorCode.INVALID_ACCESS_TOKEN;

        String traceId = MDC.get("traceId");
        log.error("[TraceID: {}] API Error: {} - Detail: {}",
                traceId, request.getRequestURI(), authException.getMessage(), authException);

        ApiErrorResponse body = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ec.getStatus().value())
                .error(ec.getStatus().getReasonPhrase())
                .errorCode(ec.getCode())
                .message(ec.getDefaultMessage())
                .path(request.getRequestURI())
                .traceId(MDC.get("traceId"))
                .build();

        response.setStatus(ec.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
