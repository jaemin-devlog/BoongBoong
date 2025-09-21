package org.hanseo.boongboong.domain.mypage.dto.response;

import java.util.List;
/**
 * 마이페이지 메인 응답 DTO.
 * - 프로필/진행중/다가올/내 게시글 목록으로 구성된 집합 응답
 */
public record MyPageRes(
        ProfileCardRes profile,            // 프로필 카드
        OngoingCarpoolRes ongoingCarpool,  // 진행중 카풀 (null 가능)
        OngoingCarpoolRes upcomingCarpool, // 예정된 카풀 (null 가능)
        List<MyPostRes> myPosts            // 내가 올린 글 목록
) {}
