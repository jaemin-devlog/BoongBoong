// domain/auth/dto/LoginResponse.java
package org.hanseo.boongboong.domain.auth.dto;


public record LoginResponse(
        Long id,          // 사용자 ID (DB PK) → 관리자 계정일 경우 null 반환 가능
        String email,     // 사용자 이메일
        String nickname,  // 사용자 닉네임 (관리자 계정일 경우 "admin" 고정)
        String role       // 사용자 권한 ("ADMIN" 또는 "USER")
) {}
