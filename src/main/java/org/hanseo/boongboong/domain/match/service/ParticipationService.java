// src/main/java/org/hanseo/boongboong/domain/match/service/ParticipationService.java
package org.hanseo.boongboong.domain.match.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.match.entity.Match;
import org.hanseo.boongboong.domain.match.entity.MatchMember;
import org.hanseo.boongboong.domain.match.repository.MatchMemberRepo;
import org.hanseo.boongboong.domain.match.repository.MatchRepo;
import org.hanseo.boongboong.domain.match.type.AttendanceStatus;
import org.hanseo.boongboong.domain.match.type.MatchStatus;
import org.hanseo.boongboong.domain.trust.entity.TrustPointHistory;
import org.hanseo.boongboong.domain.trust.repository.TrustPointHistoryRepo;
import org.hanseo.boongboong.domain.trust.type.TrustReason;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipationService {

    private final MatchRepo matchRepo;
    private final MatchMemberRepo memberRepo;
    private final TrustPointHistoryRepo trustRepo;

    /** 라이더 본인이 자신의 참여를 취소 (당일이면 -5) */
    public void cancelAsRider(String email, Long matchId, Long memberId) {
        Match match = matchRepo.lockById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        MatchMember mm = memberRepo.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 멤버-매치 일치 검증
        if (!mm.getMatch().getId().equals(match.getId()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        if (!mm.getUser().getEmail().equals(email))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        if (mm.getAttend() == AttendanceStatus.CANCELLED)
            throw new BusinessException(ErrorCode.INVALID_STATE);

        // 좌석 회수
        if (mm.getSeats() > 0) match.releaseSeats(mm.getSeats());

        // 상태 변경
        mm.setAttend(AttendanceStatus.CANCELLED);

        // 당일 취소면 -5 벌점
        if (isSameDay(match)) {
            trustRepo.save(TrustPointHistory.builder()
                    .user(mm.getUser())
                    .match(match)
                    .member(mm)
                    .delta(-5)
                    .reason(TrustReason.SAME_DAY_CANCEL)
                    .memo("당일취소")
                    .build());
        }
        // TODO: RiderCancelEvent (웹푸시)
    }

    /** 드라이버가 특정 멤버를 노쇼로 표기 (-10) */
    public void markNoShow(String driverEmail, Long matchId, Long memberId) {
        Match match = matchRepo.lockById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        if (!match.getDriver().getEmail().equals(driverEmail))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        MatchMember mm = memberRepo.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 멤버-매치 일치 검증
        if (!mm.getMatch().getId().equals(match.getId()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        if (mm.getAttend() == AttendanceStatus.NO_SHOW)
            throw new BusinessException(ErrorCode.INVALID_STATE);

        mm.setAttend(AttendanceStatus.NO_SHOW);

        trustRepo.save(TrustPointHistory.builder()
                .user(mm.getUser())
                .match(match)
                .member(mm)
                .delta(-10)
                .reason(TrustReason.NO_SHOW)
                .memo("노쇼")
                .build());
        // TODO: MarkNoShowEvent (웹푸시)
    }

    /** 드라이버가 특정 멤버를 출석 처리 */
    public void markAttended(String driverEmail, Long matchId, Long memberId) {
        Match match = matchRepo.lockById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        if (!match.getDriver().getEmail().equals(driverEmail))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        MatchMember mm = memberRepo.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));

        // 멤버-매치 일치 검증
        if (!mm.getMatch().getId().equals(match.getId()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        if (mm.getAttend() == AttendanceStatus.ATTENDED)
            throw new BusinessException(ErrorCode.INVALID_STATE);

        mm.setAttend(AttendanceStatus.ATTENDED);
        // TODO: MarkAttendedEvent (웹푸시)
    }

    /** 드라이버가 매칭 전체를 완료 처리 (운행 종료) */
    public void completeMatch(String driverEmail, Long matchId) {
        Match match = matchRepo.lockById(matchId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));

        if (!match.getDriver().getEmail().equals(driverEmail))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);

        if (match.getStatus() == MatchStatus.COMPLETED)
            throw new BusinessException(ErrorCode.INVALID_STATE);

        match.complete();
        // TODO: MatchCompletedEvent (웹푸시)
    }

    // ---------
    // helpers
    // ---------
    private boolean isSameDay(Match match) {
        return match.getDate().equals(LocalDate.now());
    }
}
