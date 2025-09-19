package org.hanseo.boongboong.global.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 요청 값입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C002", "허용되지 않은 HTTP 메서드입니다."),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "대상을 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 내부 오류입니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C403", "접근 권한이 없습니다."),

    // 사용자/회원가입
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U001", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U002", "이미 사용 중인 닉네임입니다."),
    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "U003", "이메일 인증이 완료되지 않았습니다."),
    INVALID_EMAIL_DOMAIN(HttpStatus.BAD_REQUEST, "U004", "허용되지 않은 이메일 도메인입니다."),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "U005", "이메일 형식이 올바르지 않습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U404", "해당 사용자를 찾을 수 없습니다."),

    // 이메일 인증
    TOO_FREQUENT_EMAIL_REQUEST(HttpStatus.TOO_MANY_REQUESTS, "E001", "요청이 너무 잦습니다. 잠시 후 다시 시도해 주세요."),
    EMAIL_CODE_MISMATCH(HttpStatus.BAD_REQUEST, "E002", "인증 코드가 일치하지 않습니다."),
    EMAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST, "E003", "인증 코드가 만료되었습니다."),

    // 보안/인증
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "S001", "인증에 실패했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "S002", "인증이 필요합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    public String toJson() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(ErrorResponse.of(this));
        } catch (Exception e) {
            return "{\"code\":\"" + this.code + ",\"message\":\"" + this.message + "\"}";
    }
}}
