package org.hanseo.boongboong.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

//인증 메일 발송 요청 DTO
public record EmailSendRequest(
        @NotBlank(message = "이메일은 필수 입력 값입니다.")
        @Email(message = "이메일 형식에 맞지 않습니다.")
        String email
) {
}
