package org.hanseo.boongboong.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.auth.dto.request.LoginRequest;
import org.hanseo.boongboong.domain.auth.dto.request.LoginResponse;
import org.hanseo.boongboong.domain.auth.dto.request.ResetPasswordRequest;
import org.hanseo.boongboong.domain.user.dto.request.EmailSendRequest;
import org.hanseo.boongboong.domain.user.dto.request.EmailVerifyRequest;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.domain.user.service.EmailService;
import org.hanseo.boongboong.domain.user.service.UserService;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserService userService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    /**
     * 재설정 코드 발송 (가입자 존재 확인 후 발송)
     */
    @Transactional
    public void requestPasswordReset(EmailSendRequest req) {
        String email = req.email();
        if (!userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        emailService.sendPasswordResetEmail(email);
    }

    /**
     * 재설정 코드 검증
     */
    @Transactional(readOnly = true)
    public boolean verifyPasswordResetCode(EmailVerifyRequest req) {
        return emailService.verifyPasswordResetCode(req.email(), req.code());
    }

    /**
     * 실제 비밀번호 재설정
     */
    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        String email = req.email();
        if (!emailService.isPasswordResetVerified(email)) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        userService.resetPassword(email, req.newPassword()); // 암호화 저장
        emailService.clearPasswordResetFlag(email);       // 플래그 정리
    }

    /**
     * 로그인 처리
     * 1) 인증 (실패 시 AUTHENTICATION_FAILED로 변환)
     * 2) SecurityContext 생성 및 설정
     * 3) 세션 생성 + 세션고정 보호(changeSessionId)
     * 4) 세션에 SecurityContext 저장(핵심)
     * 5) 응답 DTO 반환
     */
    @Transactional
    public LoginResponse login(LoginRequest req, HttpServletRequest httpReq) {
        final Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.username(), req.password())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            // UserService와 동일한 패턴: 도메인 예외로 변환
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);

        HttpSession session = httpReq.getSession(true);
        httpReq.changeSessionId(); // 세션 고정 공격 방지
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context
        );
        return toLoginResponse(auth);
    }

    /** 현재 로그인 사용자 정보 조회 */
    public LoginResponse getCurrentUser(Authentication authentication) {
        return toLoginResponse(authentication);
    }

    // -----------------------------------------------------
    // 내부 공통 매퍼 (이메일만 사용)
    // -----------------------------------------------------
    private LoginResponse toLoginResponse(Authentication authentication) {
        String principal = authentication.getName(); // 우리 시스템에서는 이메일을 로그인 식별자로 사용

        // 이메일로만 조회
        User user = userRepository.findByEmail(principal)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        return new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name()
        );
    }
}
