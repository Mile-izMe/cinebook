package com.cinebook.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // --- Common Error System ---
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYS-500", "System error not defined!"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "SYS-400", "Input not valid!"),
    CONFLICT_ERROR(HttpStatus.CONFLICT, "SYS-409", "Data has conflicts!"),

    // --- Business Error ---
    // --- Auth / user errors
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-001", "User not found in system"),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "USR-002", "Email already existed in system"),
    PHONE_ALREADY_EXISTS(HttpStatus.CONFLICT, "USR-003", "Phone already existed in system"),
    INVALID_VERIFY_TOKEN(HttpStatus.BAD_REQUEST, "USR-004", "Token is invalid"),
    INVALID_CREDENTIALS(HttpStatus.BAD_REQUEST, "USR-005", "Email or password is incorrect"),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "USR-006", "Email not verified yet"),
    INVALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "USR-007", "Invalid access token"),
    ACCESS_DENIED(HttpStatus.BAD_REQUEST, "USR-008", "You do not have permission to this function"),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "USR-009", "Invalid refresh token"),
    REFRESH_TOKEN_EXPIRED(HttpStatus.BAD_REQUEST, "USR-010", "Refresh token has expired"),

    // --- Movie module errors ---
    MOVIE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOV-001", "Movie not found"),
    GENRE_NOT_FOUND(HttpStatus.NOT_FOUND, "MOV-002", "Genre not found"),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "MOV-003", "You have already reviewed this movie"),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "MOV-004", "Invalid file type, only image files are allowed"),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, "MOV-005", "File size exceeds the allowed limit"),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "MOV-006", "Failed to upload file, please try again"),

    // --- City module errors ---
    CITY_NOT_FOUND(HttpStatus.NOT_FOUND, "CIN-001", "City not found"),
    CINEMA_NOT_FOUND(HttpStatus.NOT_FOUND, "CIN-002", "Cinema not found"),
    ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CIN-003", "Screening room not found"),
    SEATS_ALREADY_GENERATED(HttpStatus.CONFLICT, "CIN-004", "Seats have already been generated for this room, cannot regenerate"),
    SHOWTIME_NOT_FOUND(HttpStatus.NOT_FOUND, "CIN-005", "Showtime not found"),
    SHOWTIME_OVERLAP(HttpStatus.CONFLICT, "CIN-006", "Showtime overlaps with another showtime in the same room"),

    // --- Booking business errors ---
    SHOWTIME_ALREADY_STARTED(HttpStatus.BAD_REQUEST, "BOOK-001", "The showtime has already started, tickets cannot be booked"),
    SEAT_NOT_IN_ROOM(HttpStatus.BAD_REQUEST, "BOOK-002", "The seat does not belong to the room for this showtime"),
    SEAT_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK-003", "Seat not found"),
    SEAT_ALREADY_BOOKED(HttpStatus.CONFLICT, "BOOK-004", "The seat has already been booked by someone else"),
    BOOKING_NOT_FOUND(HttpStatus.NOT_FOUND, "BOOK-005", "Booking not found"),
    BOOKING_ACCESS_DENIED(HttpStatus.FORBIDDEN, "BOOK-006", "You do not have permission to access this booking"),
    INVALID_BOOKING_TRANSITION(HttpStatus.BAD_REQUEST, "BOOK-007", "Invalid booking status transition"),
    GUEST_INFO_REQUIRED(HttpStatus.BAD_REQUEST, "BOOK-008", "You need to provide email and phone number"),

    // --- Lock Seat errors ---
    SEAT_ALREADY_LOCKED(HttpStatus.CONFLICT, "LOCK-001", "This seat is currently held by another user"),
    SEAT_LOCK_NOT_OWNED(HttpStatus.FORBIDDEN, "LOCK-002", "You do not own the lock for this seat, or the lock has expired"),
    SEAT_LOCK_EXPIRED(HttpStatus.GONE, "LOCK-003", "The hold time for this seat has expired");
    
    private final HttpStatus status;
    private final String code;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String code, String defaultMessage) {
        this.status = status;
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
}
