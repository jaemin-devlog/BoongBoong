package org.hanseo.boongboong.domain.user.dto.request;

import jakarta.validation.constraints.*;

// 회원가입 DTO
public record SignUpRequestDto(
        @NotBlank @Size(min = 8, max = 64) String password,
        @NotBlank @Size(min = 2, max = 10) String name,      // 2~10자
        @NotBlank @Size(min = 2, max = 10) String nickname,  // 2~10자 (정책 통일)
        @NotNull @Min(18) @Max(30) Integer age,              // 18~30살 (주석 정합화)
        @NotBlank @Email String email
) {}

