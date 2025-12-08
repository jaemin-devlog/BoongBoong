// src/main/java/org/hanseo/boongboong/domain/match/controller/ParticipationController.java
package org.hanseo.boongboong.domain.match.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.match.dto.ParticipationDtos.*;
import org.hanseo.boongboong.domain.match.service.ParticipationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/match/participation")
public class ParticipationController {

    private final ParticipationService participationService;

    /** 라이더 본인 취소 (당일이면 -5) */
    @PostMapping("/rider/cancel")
    public ResponseEntity<Void> riderCancel(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody RiderCancelReq req
    ) {
        participationService.cancelAsRider(email, req.matchId(), req.memberId());
        return ResponseEntity.ok().build();
    }

    /** 드라이버가 특정 멤버 노쇼 처리 (-10) */
    @PostMapping("/driver/no-show")
    public ResponseEntity<Void> noShow(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody NoShowReq req
    ) {
        participationService.markNoShow(email, req.matchId(), req.memberId());
        return ResponseEntity.ok().build();
    }

    /** 드라이버가 특정 멤버 출석 처리 */
    @PostMapping("/driver/attend")
    public ResponseEntity<Void> attend(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody AttendReq req
    ) {
        participationService.markAttended(email, req.matchId(), req.memberId());
        return ResponseEntity.ok().build();
    }

    /** 드라이버가 매칭 종료 처리 */
    @PostMapping("/driver/complete")
    public ResponseEntity<Void> complete(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody CompleteMatchReq req
    ) {
        participationService.completeMatch(email, req.matchId());
        return ResponseEntity.ok().build();
    }

    /** 라이더가 본인 동행 완료 확인 */
    @PostMapping("/rider/complete")
    public ResponseEntity<Void> riderComplete(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody RiderCompleteReq req
    ) {
        participationService.completeAsRider(email, req.matchId());
        return ResponseEntity.ok().build();
    }
}
