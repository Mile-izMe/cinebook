package com.cinebook.common.exception;

import lombok.Getter;

@Getter
public class CinebookException extends RuntimeException {

    // Whenever business logic is wrong (ex: out of desk, wrong password)
    // Throw this class

    private final ErrorCode errorCode;

    public CinebookException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public CinebookException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
    }
}