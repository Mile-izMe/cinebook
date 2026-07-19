package com.cinebook.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // --- Common Error System ---
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-500", "Lỗi hệ thống không xác định"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "SYS-400", "Dữ liệu đầu vào không hợp lệ"),
    CONFLICT_ERROR(HttpStatus.CONFLICT, "SYS-409", "Dữ liệu gặp xung đột"),

    // --- Business Error ---
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-001", "Không tìm thấy người dùng"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USR-002", "Email đã tồn tại trong hệ thống"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "USR-003", "Phone is already existed in system"),
    INVALID_VERIFY_TOKEN(HttpStatus.BAD_REQUEST, "USR-004", "Token is invalid"),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "USR-005", "Email or password is incorrect"),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "USR-006", "Email not verified yet"),
    INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "USR-007", "Invalid access token"),
    ACCESS_DENIED(HttpStatus.BAD_REQUEST, "USR-008", "You do not have permission to this function");

    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
