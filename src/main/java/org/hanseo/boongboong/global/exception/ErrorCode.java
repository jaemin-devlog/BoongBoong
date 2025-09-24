// src/main/java/org/hanseo/boongboong/global/exception/ErrorCode.java
package org.hanseo.boongboong.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 공통
    INVALID_INPUT("INVALID_INPUT", HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    METHOD_NOT_ALLOWED("METHOD_NOT_ALLOWED", HttpStatus.METHOD_NOT_ALLOWED, "허용되지 않은 HTTP 메서드입니다."),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    INTERNAL_ERROR("INTERNAL_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다."),
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // 사용자/회원가입
    EMAIL_ALREADY_EXISTS("EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    NICKNAME_ALREADY_EXISTS("NICKNAME_ALREADY_EXISTS", HttpStatus.CONFLICT, "이미 사용 중인 닉네임입니다."),
    EMAIL_NOT_VERIFIED("EMAIL_NOT_VERIFIED", HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),
    INVALID_EMAIL_DOMAIN("INVALID_EMAIL_DOMAIN", HttpStatus.BAD_REQUEST, "허용되지 않은 이메일 도메인입니다."),
    INVALID_EMAIL_FORMAT("INVALID_EMAIL_FORMAT", HttpStatus.BAD_REQUEST, "이메일 형식이 올바르지 않습니다."),
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "해당 사용자를 찾을 수 없습니다."),
    INVALID_NICKNAME("INVALID_NICKNAME", HttpStatus.BAD_REQUEST, "닉네임이 유효하지 않습니다. 2~10자 범위로 입력하세요."),

    // 이메일 인증
    TOO_FREQUENT_EMAIL_REQUEST("TOO_FREQUENT_EMAIL_REQUEST", HttpStatus.TOO_MANY_REQUESTS, "요청이 너무 잦습니다."),
    EMAIL_CODE_MISMATCH("EMAIL_CODE_MISMATCH", HttpStatus.BAD_REQUEST, "인증 코드가 일치하지 않습니다."),
    EMAIL_CODE_EXPIRED("EMAIL_CODE_EXPIRED", HttpStatus.BAD_REQUEST, "인증 코드가 만료되었습니다."),

    // 보안/인증
    AUTHENTICATION_FAILED("AUTHENTICATION_FAILED", HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    UNAUTHORIZED("UNAUTHORIZED", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),

    // 카풀
    INVALID_DATE_TIME("INVALID_DATE_TIME", HttpStatus.BAD_REQUEST, "과거 날짜 또는 시간으로 카풀을 등록할 수 없습니다."),
    INVALID_SEAT_COUNT("INVALID_SEAT_COUNT", HttpStatus.BAD_REQUEST, "좌석 수는 1에서 8 사이여야 합니다."),

    // 매칭/요청
    POST_NOT_FOUND("POST_NOT_FOUND", HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    MATCH_NOT_FOUND("MATCH_NOT_FOUND", HttpStatus.NOT_FOUND, "매칭을 찾을 수 없습니다."),
    MATCH_REQUEST_NOT_FOUND("MATCH_REQUEST_NOT_FOUND", HttpStatus.NOT_FOUND, "매칭 요청을 찾을 수 없습니다."),
    INVALID_REQUEST("INVALID_REQUEST", HttpStatus.BAD_REQUEST, "요청 형식이 올바르지 않거나 허용되지 않습니다."),
    INVALID_STATE("INVALID_STATE", HttpStatus.CONFLICT, "현재 상태에서 수행할 수 없는 동작입니다."),
    SEAT_NOT_AVAILABLE("SEAT_NOT_AVAILABLE", HttpStatus.CONFLICT, "요청 좌석 수가 남은 좌석을 초과합니다."),
    MATCH_FULL("MATCH_FULL", HttpStatus.CONFLICT, "이미 좌석이 가득 찼습니다."),
    DUPLICATE_MEMBER("DUPLICATE_MEMBER", HttpStatus.CONFLICT, "이미 매칭에 참여 중입니다."),
    OFFER_SEATS_REQUIRED("OFFER_SEATS_REQUIRED", HttpStatus.BAD_REQUEST, "드라이버 좌석 수가 필요합니다.");

    private final String code;
    private final HttpStatus status;
    private final String message;
}
