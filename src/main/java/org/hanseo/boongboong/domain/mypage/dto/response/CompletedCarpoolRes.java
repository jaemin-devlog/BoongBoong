package org.hanseo.boongboong.domain.mypage.dto.response;

import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 마이페이지에서 노출하는 "완료된 카풀" 한 건의 요약 응답 DTO.
 * - 사용처: 과거 카풀 내역(완료 탭 등) 목록/상세 카드
 * - 포함 정보: 게시글 ID, 내 역할, 출발/도착지, 날짜, 시간
 */
public record CompletedCarpoolRes(
        Long postId,        // 게시글 ID
        PostRole myRole,    // 해당 카풀에서 나의 역할 (운전자/탑- 탑승자)
        String origin,      // 출발지
        String destination, // 목적지
        LocalDate date,     // 카풀 날짜
        LocalTime time,      // 카풀 시간
        String profileImageUrl,
        String name,
        String nickname,
        String memo,
        Integer trustPoint
) { }
