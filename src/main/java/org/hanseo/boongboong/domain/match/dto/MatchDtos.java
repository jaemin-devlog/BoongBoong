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
            SimpleUser receiver, // 게시글 작성자(수신자)
            int seats,
            String status,       // PENDING | APPROVED | ...
            String createdAt
    ) {}

    public record MyMatchRes(
            Long matchId,
            String date,
            String time,
            String from,         // match.fromKey (normalized)
            String to,           // match.toKey (normalized)
            String status,       // OPEN | LOCKED | COMPLETED | CANCELLED
            String myRole,       // DRIVER | RIDER
            int mySeats,         // my reserved/owned seats (driver=0)
            int totalSeats,
            int bookedSeats,
            String fromName,     // human-friendly origin (optional)
            String toName        // human-friendly destination (optional)
    ) {}

    public record MemberRes(
            Long memberId,
            Long userId,
            String nick,
            String role,         // DRIVER | RIDER
            int seats,
            String attend,       // UNKNOWN | ATTENDED | NO_SHOW | CANCELLED
            String profileImageUrl // profile image (Data URL or URL)
    ) {}

    public record CountersRes(
            long incomingPending,
            long sentPending
    ) {}
}
