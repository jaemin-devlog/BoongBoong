package org.hanseo.boongboong.domain.mypage.dto.response;

import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.carpool.type.PostStatus;

import java.time.LocalDate;
import java.time.LocalTime;
/**
 * 마이페이지: 내가 올린 게시글 카드 한 건의 응답 DTO.
 * - 사용처: /api/mypage/posts 목록 항목
 */
public record MyPostRes(
        Long postId,          // 게시글 ID
        PostRole type,        // 게시글 종류 (운전자/탑승자)
        PostStatus status,    // 카풀 모집 상태
        String origin,        // 출발지
        String destination,   // 목적지
        LocalDate date,       // 카풀 날짜
        LocalTime time,       // 카풀 시간
        int seats,            // 모집 좌석 (운전자 게시글인 경우)
        String profileImageUrl,
        String nickname,
        String memo,
        Integer trustPoint
) { }
