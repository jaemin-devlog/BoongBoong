package org.hanseo.boongboong.domain.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    USER("USER", "일반 사용자"),
    DRIVER("DRIVER", "운전자"),
    ADMIN("ADMIN", "관리자");

    private final String authority;   // 권한 코드 (스프링 시큐리티에서 ROLE_ 접두사와 함께 사용됨)
    private final String description; // 권한 설명 (한글로 역할 의미를 표시)
}

