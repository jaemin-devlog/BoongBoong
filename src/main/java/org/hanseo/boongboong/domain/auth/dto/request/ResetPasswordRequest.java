package org.hanseo.boongboong.domain.auth.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank
        @Size(min = 8, max = 64)
        @Pattern(
                regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\\W_]).{8,64}$",
                message = "비밀번호는 영문/숫자/특수문자 조합 8~64자여야 합니다."
        )
        String newPassword,
        @NotBlank @Size(min = 8, max = 64) String confirmPassword
        ,
        @Pattern(regexp = "^$|^[0-9]{6}$", message = "resetCode는 6자리 숫자여야 합니다.")
        String resetCode // 선택: verify-code 선행 강제 시 생략 가능
) {
    @AssertTrue(message = "비밀번호와 확인 값이 일치해야 합니다.")
    public boolean isPasswordConfirmed() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}
