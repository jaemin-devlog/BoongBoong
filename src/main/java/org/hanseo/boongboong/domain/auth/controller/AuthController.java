package org.hanseo.boongboong.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.auth.dto.request.LoginRequest;
import org.hanseo.boongboong.domain.auth.dto.LoginResponse;
import org.hanseo.boongboong.domain.auth.dto.request.ResetPasswordRequest;
import org.hanseo.boongboong.domain.auth.service.AuthService;
import org.hanseo.boongboong.domain.user.dto.request.EmailSendRequest;
import org.hanseo.boongboong.domain.user.dto.request.EmailVerifyRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
                                               HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorBody("Unauthorized", "/api/auth/me", 401));
        }
        return ResponseEntity.ok(authService.getCurrentUser(authentication));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
    /** 비밀번호 재설정 코드 발송 */
    @PostMapping("/password/reset-code")
    public ResponseEntity<MessageResponse> sendResetCode(@RequestBody @jakarta.validation.Valid EmailSendRequest req) {
        authService.requestPasswordReset(req);
        return ResponseEntity.ok(new MessageResponse("RESET_CODE_SENT"));
    }

    /** 비밀번호 재설정 코드 검증 */
    @PostMapping("/password/verify-code")
    public ResponseEntity<MessageResponse> verifyResetCode(@RequestBody @jakarta.validation.Valid EmailVerifyRequest req) {
        boolean ok = authService.verifyPasswordResetCode(req);
        return ResponseEntity.ok(new MessageResponse(ok ? "CODE_VERIFIED" : "CODE_INVALID"));
    }

    /** 비밀번호 재설정 실행 */
    @PostMapping("/password/reset")
    public ResponseEntity<MessageResponse> resetPassword(@RequestBody @jakarta.validation.Valid ResetPasswordRequest req,
                                                         HttpServletRequest request) {
        authService.resetPassword(req);
        // 현재 세션 무효화 및 컨텍스트 클리어 (전역 세션 종료는 Spring Session/Redis 권장)
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("PASSWORD_RESET_DONE"));
    }

    /** 간단 성공 응답 */
    private record MessageResponse(String message) {}
    private record ErrorBody(String message, String path, int status) {}
}
