package org.hanseo.boongboong.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.hanseo.boongboong.domain.auth.dto.LoginRequest;
import org.hanseo.boongboong.domain.auth.dto.LoginResponse;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public AuthController(AuthenticationConfiguration authenticationConfiguration, UserRepository userRepository) throws Exception {
        // AuthenticationManager 주입 (스프링 시큐리티 인증 핵심 객체)
        this.authenticationManager = authenticationConfiguration.getAuthenticationManager();
        this.userRepository = userRepository;
    }

    /**
     * 로그인 API
     * - 클라이언트가 username(이메일/관리자ID), password를 JSON으로 보냄
     * - AuthenticationManager가 DB/시큐리티를 통해 인증 처리
     * - 인증 성공 시 SecurityContextHolder에 Authentication 저장
     * - 세션 생성(JSESSIONID 쿠키 발급) + 세션 ID 갱신 (세션 고정 공격 방어)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest req, HttpServletRequest httpReq) {
        // AuthenticationManager를 이용해 인증 시도 (UserDetailsService + PasswordEncoder 동작)
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );

        // 인증 성공 시 SecurityContext에 저장 → 이후 요청에서 principal, authorities 확인 가능
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 세션을 강제로 생성 (없으면 새로 만듦)
        HttpSession session = httpReq.getSession(true);

        // 세션 고정 공격(Session Fixation Attack) 방어 → 로그인 성공 시 세션 ID를 새로 발급
        httpReq.changeSessionId();

        // 권한 목록 중 ROLE_ADMIN이 있으면 관리자 계정
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            // 관리자: DB에 존재하면 DB정보 반환, 없으면 "admin" 계정으로 간주
            return userRepository.findByEmail(req.username())
                    .map(u -> ResponseEntity.ok(new LoginResponse(u.getId(), u.getEmail(), u.getNickname(), u.getRole().name())))
                    .orElseGet(() -> ResponseEntity.ok(new LoginResponse(null, req.username(), "admin", "ADMIN")));
        }

        // 일반 사용자: DB에서 정보 조회 후 반환
        User u = userRepository.findByEmail(req.username()).orElseThrow();
        return ResponseEntity.ok(new LoginResponse(u.getId(), u.getEmail(), u.getNickname(), u.getRole().name()));
    }

    /**
     * 현재 로그인 사용자 조회 API
     * - 세션에 저장된 SecurityContext 기반으로 Principal 객체 획득
     * - principal이 null이면 인증되지 않은 상태 → 401 반환
     */
    @GetMapping("/me")
    public ResponseEntity<LoginResponse> me(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        String email = principal.getName(); // 현재 로그인 사용자의 username (email)
        // 관리자 계정일 수도 있으므로 분기 처리
        return userRepository.findByEmail(email)
                .map(u -> ResponseEntity.ok(new LoginResponse(u.getId(), u.getEmail(), u.getNickname(), u.getRole().name())))
                .orElseGet(() -> ResponseEntity.ok(new LoginResponse(null, email, "admin", "ADMIN")));
    }

    /**
     * 로그아웃 API
     * - 현재 요청과 연결된 세션 무효화
     * - SecurityContextHolder 비우기 → 인증 정보 제거
     * - 응답: 204 No Content
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false); // 이미 존재하는 세션만 가져옴 (없으면 null)
        if (session != null) session.invalidate();   // 세션 무효화 (JSESSIONID 삭제)
        SecurityContextHolder.clearContext();        // SecurityContext도 초기화
        return ResponseEntity.noContent().build();
    }
}
