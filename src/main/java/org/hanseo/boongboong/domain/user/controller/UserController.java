package org.hanseo.boongboong.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.user.dto.request.EmailVerifyRequest;
import org.hanseo.boongboong.domain.user.dto.request.EmailSendRequest;
import org.hanseo.boongboong.domain.user.dto.request.SignUpRequestDto;
import org.hanseo.boongboong.domain.user.dto.response.EmailSendRes;
import org.hanseo.boongboong.domain.user.dto.response.EmailVerifyRes;
import org.hanseo.boongboong.domain.user.dto.response.SignUpRes;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.mapper.UserMapper;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.domain.user.service.EmailService;
import org.hanseo.boongboong.domain.user.service.UserService;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 계정/이메일 인증 컨트롤러.
 * - 회원가입, 인증 메일 발송/검증
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;       // 회원가입 로직
    private final EmailService emailService;     // 이메일 인증 로직
    private final UserRepository userRepository; // 저장된 사용자 조회

    /** 회원가입: 201 + SignUpRes JSON 본문 반환 */
    @PostMapping("/signup")
    public ResponseEntity<SignUpRes> signUp(@Valid @RequestBody SignUpRequestDto dto) {
        Long userId = userService.signup(dto); // 가입 처리(아바타 생성 포함)
        User saved = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // 저장 검증
        SignUpRes body = UserMapper.toSignUpRes(saved); // 응답 매핑
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /** 인증 메일 발송: 200 + JSON 바디 */
    @PostMapping("/email/request")
    public ResponseEntity<EmailSendRes> requestVerificationEmail(@RequestBody @Valid EmailSendRequest requestDto) {
        emailService.sendVerificationEmail(requestDto.email()); // 메일 발송
        EmailSendRes res = EmailSendRes.builder()
                .sent(true)
                .cooldownSeconds(60)  // 재요청 쿨다운(초) - 서비스의 1분 쿨다운과 일치
                .ttlSeconds(300)      // 코드 유효기간(초)
                .message("인증 코드를 전송했습니다.")
                .build();
        return ResponseEntity.ok(res);
    }

    /** 인증 코드 검증: 200 + JSON 바디 */
    @PostMapping("/email/verify")
    public ResponseEntity<EmailVerifyRes> verifyEmail(@RequestBody @Valid EmailVerifyRequest requestDto) {
        emailService.verifyEmail(requestDto.email(), requestDto.code()); // 코드 검증
        EmailVerifyRes res = EmailVerifyRes.builder()
                .verified(true)
                .message("이메일 인증이 완료되었습니다.")
                .build();
        return ResponseEntity.ok(res);
    }
}
