package org.hanseo.boongboong.domain.mypage.controller;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.mypage.dto.response.*;
import org.hanseo.boongboong.domain.mypage.service.MyPageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
/**
 * 마이페이지 HTTP 엔드포인트 컨트롤러.
 * - /api/mypage 하위의 조회/업서트/삭제 요청 처리
 * - 이메일로 사용자 식별(추후 토큰 기반으로 전환 가능)
 */
@RestController // REST 컨트롤러 선언
@RequiredArgsConstructor // 생성자 주입
@RequestMapping("/api/mypage") // 기본 URL prefix
public class MyPageController {

    private final MyPageService myPageService; // 서비스 의존성

    /** 내 정보 전체(프로필 카드 + 진행 1건 + 다가올 1건 + 내 글 50) */
    @GetMapping("/me")
    public ResponseEntity<MyPageRes> me(@RequestParam String email) { // 쿼리파라미터로 이메일 수신
        return ResponseEntity.ok(myPageService.getMyPageInfo(email)); // 서비스 위임
    }

    /** 프로필 카드만 */
    @GetMapping("/profile")
    public ResponseEntity<ProfileCardRes> profile(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.profile(email));
    }

    /** 진행중 카풀 1건 (매칭 도메인 붙기 전까지는 null) */
    @GetMapping("/ongoing")
    public ResponseEntity<OngoingCarpoolRes> ongoing(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.ongoing(email));
    }

    /** 다가올(예정) 카풀 1건 */
    @GetMapping("/upcoming")
    public ResponseEntity<OngoingCarpoolRes> upcoming(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.upcoming(email));
    }

    /** 다가올(예정) 카풀 전체 리스트 */
    @GetMapping("/upcoming/list")
    public ResponseEntity<List<OngoingCarpoolRes>> upcomingList(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.upcomingList(email));
    }

    /** 내가 올린 글 목록(최신 50) */
    @GetMapping("/posts")
    public ResponseEntity<List<MyPostRes>> myPosts(@RequestParam String email) {
        return ResponseEntity.ok(myPageService.myPosts(email));
    }

    /** 차량 등록/수정(Upsert) */
    @PostMapping("/car")
    public ResponseEntity<ProfileCardRes> upsertVehicle(@RequestParam String email,
                                                        @RequestBody VehicleReq req) { // 요청 바디 record
        return ResponseEntity.ok(myPageService.upsertVehicle(email, req.number(), req.imageUrl()));
    }

    /** 차량 삭제 */
    @DeleteMapping("/car")
    public ResponseEntity<Void> deleteVehicle(@RequestParam String email) {
        myPageService.deleteVehicle(email);
        return ResponseEntity.noContent().build(); // 204
    }

    // === 요청 바디 DTO(간단히 컨트롤러 안에 record로 정의) ===
    public record NicknameReq(String nickname) {} // 닉네임 변경 요청 바디
    public record VehicleReq(String number, String imageUrl) {} // 차량 업서트 요청 바디
}
