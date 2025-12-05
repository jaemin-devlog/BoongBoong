package org.hanseo.boongboong.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hanseo.boongboong.domain.user.dto.request.UpdateNicknameRequest;
import org.hanseo.boongboong.domain.user.dto.request.UpdateOpenChatUrlRequest;
import org.hanseo.boongboong.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자 유틸리티(중복 확인/닉네임 변경) 컨트롤러.
 * - 인증 불필요 API와 인증 필요 API를 함께 제공
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserUtilController {

    private final UserService userService;

    /**
     * 닉네임 중복 여부 조회 (비로그인 허용).
     * - 예: GET /api/users/nickname/exists?nickname=붕붕
     */
    @GetMapping("/nickname/exists")
    public ResponseEntity<CheckResponse> existsNickname(@RequestParam String nickname) {
        boolean exists = userService.isNicknameExists(nickname); // 서비스에 중복 조회 위임
        return ResponseEntity.ok(new CheckResponse(exists));     // 단순 불리언 래핑하여 응답
    }

    /**
     * 이메일 중복 여부 조회 (비로그인 허용).
     * - 예: GET /api/users/email/exists?email=a@b.com
     */
    @GetMapping("/email/exists")
    public ResponseEntity<CheckResponse> existsEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email); // 서비스 위임
        return ResponseEntity.ok(new CheckResponse(exists));
    }

    /**
     * 닉네임 변경 (인증 사용자 / SecurityContext 기반).
     * - 인증 토큰의 username=email을 꺼내 식별자로 사용
     */
    @PutMapping("/nickname")
    public void updateNickname(
            @AuthenticationPrincipal UserDetails principal, // 인증 사용자(스프링 시큐리티)
            @Valid @RequestBody UpdateNicknameRequest req   // 유효성 검증된 요청 바디
    ) {
        String email = principal.getUsername();             // username=email 사용
        userService.updateNicknameByEmail(email, req.nickname()); // 서비스에 변경 위임
        log.info("[UserUtilController] 닉네임 변경 완료. email={}, newNickname={}", email, req.nickname());
    }

    /** 간단 응답 DTO(내부 전용) */
    @PutMapping("/openchat")
    public ResponseEntity<Void> updateOpenChatUrl(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody UpdateOpenChatUrlRequest req
    ) {
        String email = principal.getUsername();
        userService.updateOpenChatUrlByEmail(email, req.openChatUrl());
        log.info("[UserUtilController] 오픈채팅 URL 업데이트. email={}", email);
        return ResponseEntity.ok().build();
    }

    private record CheckResponse(boolean exists) {}
}
