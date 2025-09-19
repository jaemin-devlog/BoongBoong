package org.hanseo.boongboong.global.exception;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ErrorResponse {
    private final String message;
    private final String code;

    @Builder
    public ErrorResponse(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .message(errorCode.getMessage())
                .code(errorCode.getCode())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .message(message)
                .code(errorCode.getCode())
                .build();
    }
}

