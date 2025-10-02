package org.hanseo.boongboong.domain.match.dto;

import jakarta.validation.constraints.NotNull;

public class ParticipationDtos {

    // 라이더 본인 취소 요청
    public record RiderCancelReq(
            @NotNull Long matchId,
            @NotNull Long memberId
    ) {}

    // 드라이버가 특정 멤버 노쇼 처리
    public record NoShowReq(
            @NotNull Long matchId,
            @NotNull Long memberId
    ) {}

    // 드라이버가 특정 멤버 출석 처리
    public record AttendReq(
            @NotNull Long matchId,
            @NotNull Long memberId
    ) {}

    // 드라이버가 매칭 완료(운행 종료)
    public record CompleteMatchReq(
            @NotNull Long matchId
    ) {}
}
