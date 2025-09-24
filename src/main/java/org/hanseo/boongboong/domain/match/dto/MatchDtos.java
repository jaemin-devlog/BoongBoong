// src/main/java/org/hanseo/boongboong/domain/match/dto/MatchDtos.java
package org.hanseo.boongboong.domain.match.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class MatchDtos {
    public record RequestCreateReq(@NotNull Long postId, @Min(1) int seats) {}
    public record RequestIdRes(Long requestId) {}
    public record MatchIdRes(Long matchId) {}

    // 목록/요약 응답 DTO들
    public record SimpleUser(Long id, String nick) {}

    public record MatchReqSummaryRes(
            Long requestId,
            Long postId,
            String postType,     // DRIVER | RIDER
            SimpleUser requester,
            int seats,
            String status,       // PENDING | APPROVED | ...
            String createdAt
    ) {}

    public record MyMatchRes(
            Long matchId,
            String date,
            String time,
            String from,         // match.fromKey
            String to,           // match.toKey
            String status,       // OPEN | LOCKED | COMPLETED | CANCELLED
            String myRole,       // DRIVER | RIDER
            int mySeats,         // 내가 점유한 좌석(드라이버는 0)
            int totalSeats,
            int bookedSeats
    ) {}

    public record MemberRes(
            Long memberId,
            Long userId,
            String nick,
            String role,         // DRIVER | RIDER
            int seats,
            String attend        // UNKNOWN | ATTENDED | NO_SHOW | CANCELLED
    ) {}

    public record CountersRes(
            long incomingPending,
            long sentPending
    ) {}
}
