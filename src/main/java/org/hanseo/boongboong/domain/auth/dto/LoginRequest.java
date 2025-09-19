// domain/auth/dto/LoginRequest.java
package org.hanseo.boongboong.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 로그인 요청 DTO
 * - 클라이언트가 로그인 시도 시 서버로 전달하는 데이터
 * - username은 이메일(일반 사용자) 또는 admin 아이디(관리자)로 사용
 */
public record LoginRequest(
        @NotBlank String username,  // 로그인 ID (이메일 또는 관리자 아이디)
        @NotBlank String password   // 비밀번호 (평문 입력, 서버에서 PasswordEncoder로 검증)
) {}
