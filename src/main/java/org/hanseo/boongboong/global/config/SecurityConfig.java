package org.hanseo.boongboong.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.user.service.JpaUserDetailsService;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.hanseo.boongboong.global.exception.ErrorResponse;
import org.hanseo.boongboong.global.security.JsonAuthEntryPoint;
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

    /** AuthenticationManager 등록 */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    /** DB(UserDetailsService) 기반 인증 Provider */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(jpaUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /** 관리자 인메모리 계정 (옵션) */
    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager(PasswordEncoder encoder) {
        if (!adminProps.isEnabled()) {
            return new InMemoryUserDetailsManager();
        }
        String encoded = encoder.encode(adminProps.getPassword());
        UserDetails admin = User.builder()
                .username(adminProps.getUsername())
                .password(encoded)
                .roles(adminProps.getRole()) // 예: "ADMIN"
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    /** 403 JSON 응답 */
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        ObjectMapper om = new ObjectMapper();
        return (request, response, ex) -> {
            ErrorCode ec = ErrorCode.ACCESS_DENIED;
            ErrorResponse body = ErrorResponse.of(ec, request.getRequestURI());
            response.setStatus(ec.getStatus().value());
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(om.writeValueAsString(body));
        };
    }

    /** 보안 필터 체인 */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           AccessDeniedHandler accessDeniedHandler) throws Exception {

        http
                .cors(c -> {})                                   // 필요 시 CorsConfigurationSource 빈 등록
                .csrf(csrf -> csrf.disable())                    // 세션 기반 API이면 상황에 따라 켜도 됨
                .sessionManagement(sm -> sm
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)) // 세션 사용
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthEntryPoint) // 401 JSON
                        .accessDeniedHandler(accessDeniedHandler))    // 403 JSON
                .authorizeHttpRequests(auth -> auth
                        // 인증 불필요
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/logout",
                                "/api/users/email/**",
                                "/api/users/signup"
                        ).permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // 권한 필요
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/me").authenticated()
                        // 그 외 전부 인증
                        .anyRequest().authenticated())
                .httpBasic(b -> b.disable())
                .formLogin(f -> f.disable())
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
