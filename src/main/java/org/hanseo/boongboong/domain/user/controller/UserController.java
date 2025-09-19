package org.hanseo.boongboong.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.user.dto.request.EmailVerifyRequest;
import org.hanseo.boongboong.domain.user.dto.request.EmailSendRequest;
import org.hanseo.boongboong.domain.user.dto.request.SignUpRequestDto;
import org.hanseo.boongboong.domain.user.service.EmailService;
import org.hanseo.boongboong.domain.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 가입/이메일 인증 컨트롤러
 * - 이메일 인증: EmailService
 * - 회원가입: UserService
 * 실패 케이스는 서비스에서 BusinessException(ErrorCode)로 던지고,
 * GlobalExceptionHandler가 일관된 에러 응답을 내려준다.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;   //회원가입 로직
    private final EmailService emailService; // 이메일 인증 로직

    /** 회원가입 */
    @PostMapping("/signup")
    public ResponseEntity<Void> signUp(@RequestBody @Valid SignUpRequestDto signUpRequestDto) {
        userService.signup(signUpRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** 이메일 인증 코드 발송 (쿨다운/TTL 적용)
     * 1분 쿨다운, 5분 유효기간, 6자리 코드 전송
     * */
    @PostMapping("/email/request")
    public ResponseEntity<Void> requestVerificationEmail(@RequestBody @Valid EmailSendRequest requestDto) {
        emailService.sendVerificationEmail(requestDto.email());
        return ResponseEntity.ok().build();
    }

    /** 이메일 인증 코드 확인 */
    @PostMapping("/email/verify")
    public ResponseEntity<Void> verifyEmail(@RequestBody @Valid EmailVerifyRequest requestDto) {
        // 실패 시 서비스가 BusinessException을 던짐 → 전역 핸들러가 적절한 상태코드/본문 반환
        emailService.verifyEmail(requestDto.email(), requestDto.code());
        return ResponseEntity.ok().build();
    }
}
