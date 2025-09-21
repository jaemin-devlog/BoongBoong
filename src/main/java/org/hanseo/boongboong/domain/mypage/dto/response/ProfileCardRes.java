package org.hanseo.boongboong.domain.mypage.dto.response;

import org.hanseo.boongboong.domain.user.entity.Role;

/**
 * 마이페이지 상단 프로필 카드 응답 DTO.
 * - 사용자 기본 정보 + 차량 요약 정보 포함
 */
public record ProfileCardRes(
        String email,               // 사용자 식별 이메일
        String nickname,            // 닉네임
        String profileImg,          // 프로필 이미지 SVG 데이터 URL
        Role role,                  // 사용자 권한 (USER, ADMIN)
        int trustScore,             // 신뢰 점수
        boolean hasDriverLicense,   // 운전면허증 등록 여부
        VehicleInfoRes vehicleInfo  // 등록된 차량 정보 (null 가능)
) {
    public record VehicleInfoRes(
            String number,   // 차량 번호
            String imageUrl  // 차량 이미지 URL
    ) { }
}
