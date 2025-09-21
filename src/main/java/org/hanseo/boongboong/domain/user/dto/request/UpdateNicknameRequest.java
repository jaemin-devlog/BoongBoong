package org.hanseo.boongboong.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 닉네임 변경 요청 DTO.
 * - 제약: 공백 금지, 2~10자
 */
public record UpdateNicknameRequest(
        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(min = 2, max = 10, message = "닉네임은 2~10자로 입력하세요.")
        String nickname
) { }
