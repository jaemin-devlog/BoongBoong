// src/main/java/org/hanseo/boongboong/domain/match/controller/MatchController.java
package org.hanseo.boongboong.domain.match.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.match.dto.MatchDtos.*;
import org.hanseo.boongboong.domain.match.service.MatchService;
import org.hanseo.boongboong.domain.match.type.MatchStatus;
import org.hanseo.boongboong.domain.match.type.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match")
public class MatchController {

    private final MatchService matchService;

    // ---------- 생성/승인/거절/취소 ----------

    @PostMapping("/requests")
    public ResponseEntity<RequestIdRes> createRequest(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody RequestCreateReq req
    ) {
        Long id = matchService.createRequest(email, req);
        return ResponseEntity.ok(new RequestIdRes(id));
    }

    @PostMapping("/requests/{id}/approve")
    public ResponseEntity<MatchIdRes> approve(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long id
    ) {
        Long matchId = matchService.approveRequest(email, id);
        return ResponseEntity.ok(new MatchIdRes(matchId));
    }

    @PostMapping("/requests/{id}/reject")
    public ResponseEntity<Void> reject(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long id
    ) {
        matchService.rejectRequest(email, id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/requests/{id}/cancel")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long id
    ) {
        matchService.cancelRequest(email, id);
        return ResponseEntity.ok().build();
    }

    // ---------- 목록/조회 ----------

    /** 나에게 온 매칭 요청(수신함) */
    @GetMapping("/requests/incoming")
    public ResponseEntity<Page<MatchReqSummaryRes>> incoming(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam(required = false) RequestStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(matchService.getIncomingRequests(email, status, pageable));
    }

    /** 내가 보낸 매칭 요청(보낸함) */
    @GetMapping("/requests/sent")
    public ResponseEntity<Page<MatchReqSummaryRes>> sent(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam(required = false) RequestStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(matchService.getSentRequests(email, status, pageable));
    }

    /** 특정 게시글에 들어온 요청 목록(글 작성자만) */
    @GetMapping("/requests/post/{postId}")
    public ResponseEntity<Page<MatchReqSummaryRes>> forPost(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long postId,
            @RequestParam(required = false) RequestStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(matchService.getRequestsForPost(email, postId, status, pageable));
    }

    /** 내 매칭 목록(내가 멤버인 Match 들) */
    @GetMapping("/matches")
    public ResponseEntity<Page<MyMatchRes>> myMatches(
            @AuthenticationPrincipal(expression = "username") String email,
            @RequestParam(required = false) PostRole role,
            @RequestParam(required = false) MatchStatus status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(matchService.getMyMatches(email, role, status, pageable));
    }

    /** 매칭 멤버 목록(해당 매칭 멤버만 접근) */
    @GetMapping("/{matchId}/members")
    public ResponseEntity<java.util.List<MemberRes>> members(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long matchId
    ) {
        return ResponseEntity.ok(matchService.getMembers(email, matchId));
    }

    /** 해당 매칭에서의 내 멤버십(버튼에 memberId 필요할 때) */
    @GetMapping("/{matchId}/me")
    public ResponseEntity<MemberRes> me(
            @AuthenticationPrincipal(expression = "username") String email,
            @PathVariable Long matchId
    ) {
        return ResponseEntity.ok(matchService.getMe(email, matchId));
    }

    /** 대시보드 카운트(뱃지) */
    @GetMapping("/requests/counters")
    public ResponseEntity<CountersRes> counters(
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        return ResponseEntity.ok(matchService.getCounters(email));
    }
}
