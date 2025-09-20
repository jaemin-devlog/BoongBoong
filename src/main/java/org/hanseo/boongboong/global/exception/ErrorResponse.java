package org.hanseo.boongboong.global.exception;

import org.slf4j.MDC;

import java.time.OffsetDateTime;
import java.util.Map;

public record ErrorResponse(
        String code,
        String message,
        int status,
        String path,
        String timestamp,
        String traceId,
        Map<String, String> details
) {
    public static ErrorResponse of(ErrorCode ec, String path) {
        return of(ec, path, null);
    }

    public static ErrorResponse of(ErrorCode ec, String path, Map<String, String> details) {
        return new ErrorResponse(
                ec.getCode(),
                ec.getMessage(),
                ec.getStatus().value(),
                path,
                OffsetDateTime.now().toString(),
                MDC.get("traceId"),
                details
        );
    }
}
