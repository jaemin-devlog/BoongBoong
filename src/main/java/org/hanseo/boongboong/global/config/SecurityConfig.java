// src/main/java/org/hanseo/boongboong/global/config/SecurityConfig.java
package org.hanseo.boongboong.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.user.service.JpaUserDetailsService;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.hanseo.boongboong.global.exception.ErrorResponse;
import org.hanseo.boongboong.global.security.JsonAuthEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JpaUserDetailsService jpaUserDetailsService;
    private final JsonAuthEntryPoint jsonAuthEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Spring이 구성한 AuthenticationManager를 그대로 사용한다.
     * (JpaUserDetailsService + PasswordEncoder 조합으로 DaoAuthenticationProvider가 자동 구성됨)
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    /** 403 응답을 JSON으로 반환 */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        ObjectMapper om = new ObjectMapper();
        return (req, res, ex) -> {
            var ec = ErrorCode.ACCESS_DENIED;
            res.setStatus(ec.getStatus().value());
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write(om.writeValueAsString(ErrorResponse.of(ec, req.getRequestURI())));
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .cors(c -> {})
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/users/email/**",
                                "/api/users/signup",
                                "/api/users/nickname/exists",
                                "/api/auth/password/reset-code",
                                "/api/auth/password/verify-code",
                                "/api/auth/password/reset",
                                "/test-ws.html"           // 로컬 테스트 페이지 허용
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        // WebSocket 핸드셰이크 및 SockJS 리소스는 로그인 세션으로 접근(익명 불가)
                        .requestMatchers("/ws/**").authenticated()
                        .anyRequest().authenticated())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable());

        return http.build();
    }

    /**
     * CORS 설정: 데모/프론트 연동을 위해 최대 허용(Origin 패턴 *, 모든 메서드/헤더, Credentials 허용)
     * 브라우저에서는 credentials: 'include' 로 호출 필요.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Set-Cookie"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
