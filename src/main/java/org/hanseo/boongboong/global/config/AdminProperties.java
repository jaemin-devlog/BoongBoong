package org.hanseo.boongboong.global.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "admin.auth")
public class AdminProperties {
    private boolean enabled;
    private String username;
    private String password; // ⚡ 평문 비밀번호 입력
    private String role;
}


