package org.hanseo.boongboong.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 회원가입 응답 DTO.
 * - 프론트 초기 화면 구성을 위한 최소 정보 반환
 */
@Getter
@Builder
public class SignUpRes {
    private Long id;           // 사용자 ID
    private String email;      // 이메일
    private String nickname;   // 닉네임

    // 아바타/프로필
    private String profileImg; // data:image/svg+xml;utf8,...
}
