package org.hanseo.boongboong.domain.mypage.dto.response;

import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalTime;
/**
 * 마이페이지: 진행중/예정 카풀 카드 한 건의 응답 DTO.
 * - 사용처: /api/mypage/ongoing, /api/mypage/upcoming, /api/mypage/upcoming/list
 */
public record OngoingCarpoolRes(
        Long matchId,        // 매칭 ID
        Long myMemberId,     // 나의 MatchMember ID
        Long postId,         // 게시글 ID(없을 수 있음)
        Long partnerUserId,  // 상대방 사용자 ID(라이더일 때 운전자 ID)
        PostRole myRole,     // 해당 카풀에서 나의 역할 (운전자/탑승자)
        String origin,       // 출발지
        String destination,  // 목적지
        LocalDate date,      // 카풀 날짜
        LocalTime time,      // 카풀 시간
        String profileImageUrl,
        String nickname,
        String memo,
        Integer trustPoint
) { }
