package org.hanseo.boongboong.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 인증코드 발송 요청에 대한 응답 DTO.
 * - sent: 발송 성공 여부
 * - cooldownSeconds: 재요청까지 대기 시간(30초)
 * - ttlSeconds: 인증코드 유효기간(300초(5분))
 * - message: 사용자 안내 메시지(가이드/실패 사유 등)
 */
@Getter
@Builder
public class EmailSendRes {
    private boolean sent;         // 전송 성공
    private int cooldownSeconds;  // 다음 요청까지 대기(초) - 정책 없다면 0
    private int ttlSeconds;       // 인증 코드 유효기간(초) - 정책 없다면 0
    private String message;       // 사용자 메시지
}
