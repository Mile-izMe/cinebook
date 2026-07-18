package com.cinebook.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle Business error actively throw (CinebookException)
     */
    @ExceptionHandler(CinebookException.class)
    public ResponseEntity<ApiErrorResponse> handleCinebookException(CinebookException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();

        // Log warning (normally business error only need to log INFO or WARN)
        log.warn("Business Exception: {} - {}", errorCode.getCode(), ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(errorCode.getStatus().value())
                .error(errorCode.getStatus().getReasonPhrase())
                .errorCode(errorCode.getCode())
                .message(ex.getMessage())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(response, errorCode.getStatus());
    }

    /**
     * Handle system error (NullPointerException, SQLException,...)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        ErrorCode defaultError = ErrorCode.INTERNAL_SERVER_ERROR;

        // Log ERROR attach stacktrace to debug
        log.error("Internal Server Error: ", ex);

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(defaultError.getStatus().value())
                .error(defaultError.getStatus().getReasonPhrase())
                .errorCode(defaultError.getCode())
                .message(defaultError.getDefaultMessage())
                // Don't return real message of Exception (ex.getMessage()) to avoid leaking system information
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(response, defaultError.getStatus());
    }

    /**
     * Handle error when user sending missing fields
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<FieldErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new FieldErrorDetail(fe.getField(), fe.getDefaultMessage()))
                .toList();

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ErrorCode.VALIDATION_ERROR.getStatus().value())
                .error(ErrorCode.VALIDATION_ERROR.getStatus().getReasonPhrase())
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .message(ErrorCode.VALIDATION_ERROR.getDefaultMessage())
                .details(details)
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(response, ErrorCode.VALIDATION_ERROR.getStatus());
    }

    /**
     * Handle error DB unique constraint
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation: {}", ex.getMessage());

        ApiErrorResponse response = ApiErrorResponse.builder()
                .timestamp(Instant.now())
                .status(ErrorCode.CONFLICT_ERROR.getStatus().value())
                .error(ErrorCode.CONFLICT_ERROR.getStatus().getReasonPhrase())
                .errorCode(ErrorCode.CONFLICT_ERROR.getCode())
                .message(ErrorCode.CONFLICT_ERROR.getDefaultMessage())
                .path(request.getRequestURI())
                .traceId(getTraceId())
                .build();

        return new ResponseEntity<>(response, ErrorCode.CONFLICT_ERROR.getStatus());
    }

    private String getTraceId() {
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : "UNKNOWN";
    }
}
