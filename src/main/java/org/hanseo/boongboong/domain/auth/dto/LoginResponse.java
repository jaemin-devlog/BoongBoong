// domain/auth/dto/LoginResponse.java
package org.hanseo.boongboong.domain.auth.dto;

public record LoginResponse(
        Long id,          // 사용자 ID
        String email,     // 사용자 이메일
        String nickname,  // 사용자 닉네임
        String role       // 사용자 권한
) {}

