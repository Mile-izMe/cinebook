package com.cinebook.common.security;

import com.cinebook.common.exception.ApiErrorResponse;
import com.cinebook.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;

/**
 * Triggered when an authenticated user hits an endpoint they don't have the
 * role for (e.g. CUSTOMER calling /api/admin/**) -> 403.
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        ErrorCode ec = ErrorCode.ACCESS_DENIED;

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
