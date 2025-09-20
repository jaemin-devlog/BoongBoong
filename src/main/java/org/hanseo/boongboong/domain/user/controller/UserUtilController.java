package org.hanseo.boongboong.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserUtilController {

    private final UserService userService;

    /**
     * 닉네임 중복 여부 조회 (비로그인 허용)
     * GET /api/users/nickname/exists?nickname=닉
     */
    @GetMapping("/nickname/exists")
    public ResponseEntity<CheckResponse> existsNickname(@RequestParam String nickname) {
        boolean exists = userService.isNicknameExists(nickname);
        return ResponseEntity.ok(new CheckResponse(exists));
    }

    /** 이메일 중복 여부 조회 (비로그인 허용) */
    @GetMapping("/email/exists")
    public ResponseEntity<CheckResponse> existsEmail(@RequestParam String email) {
        boolean exists = userService.isEmailExists(email);
        return ResponseEntity.ok(new CheckResponse(exists));
    }

    /** 간단 응답 DTO */
    private record CheckResponse(boolean exists) {}
}
