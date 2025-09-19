package org.hanseo.boongboong.global.config;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.hanseo.boongboong.global.security.JsonAuthEntryPoint;
import org.hanseo.boongboong.domain.user.service.JpaUserDetailsService;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(AdminProperties.class)
public class SecurityConfig {

    private final AdminProperties adminProps;
    private final JpaUserDetailsService jpaUserDetailsService;
    private final JsonAuthEntryPoint jsonAuthEntryPoint;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // AuthenticationManager를 Bean으로 등록
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // DB 기반 사용자 인증 Provider
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(jpaUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    // 관리자 계정을 In-Memory에 등록 (평문 비밀번호 → bcrypt 변환)
    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder encoder) {
        if (!adminProps.isEnabled()) {
            return new InMemoryUserDetailsManager(); // admin 기능 꺼져 있으면 빈 유저 매니저
        }

        String rawPassword = adminProps.getPassword(); // 평문 비밀번호
        String encodedPassword = encoder.encode(rawPassword); // bcrypt 변환

        UserDetails adminUser = User.builder()
                .username(adminProps.getUsername())
                .password(encodedPassword)
                .roles(adminProps.getRole()) // "ADMIN"
                .build();

        return new InMemoryUserDetailsManager(adminUser);
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(ErrorCode.ACCESS_DENIED.getStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(ErrorCode.ACCESS_DENIED.toJson());
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AccessDeniedHandler accessDeniedHandler) throws Exception {
        http
                .cors(c -> {}) // 필요 시 CorsConfigurationSource 등록
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/users/email/**",
                                "/api/users/signup"
                        ).permitAll()
                        .requestMatchers("/api/auth/me").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated()
                )
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .authenticationProvider(authenticationProvider()); // DB 인증 Provider 등록

        return http.build();
    }
}
