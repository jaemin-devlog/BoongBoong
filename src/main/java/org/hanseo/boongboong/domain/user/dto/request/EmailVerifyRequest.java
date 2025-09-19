package org.hanseo.boongboong.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

//이메일 인증 확인 요청 DTO
public record EmailVerifyRequest(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식에 맞지 않습니다.")
        String email,
        @NotBlank(message = "인증 코드는 필수 입력 값입니다.")
        String code
) {
}
