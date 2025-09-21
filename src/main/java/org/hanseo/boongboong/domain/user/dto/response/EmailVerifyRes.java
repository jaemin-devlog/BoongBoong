// EmailVerifyRes.java
package org.hanseo.boongboong.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 이메일 인증코드 검증 결과 응답 DTO.
 * - verified: 검증 성공 여부
 * - message: 사용자 안내 메시지(성공/실패 사유)
 */
@Getter
@Builder
public class EmailVerifyRes {
    private boolean verified; // 검증 성공
    private String message;   // 사용자 메시지
}
