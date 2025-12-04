package org.hanseo.boongboong.domain.carpool.mapper;

import org.hanseo.boongboong.domain.carpool.dto.response.AuthorDto;
import org.hanseo.boongboong.domain.user.entity.User;

/**
 * User → AuthorDto 변환 전용 매퍼.
 * - 서비스 노출명은 닉네임만 사용.
 * - profileImg(Data URL 또는 URL)을 그대로 <img src="...">로 사용.
 * - avatarLetter, avatarColor는 부가 정보로 노출.
 */
// 역할: User 엔티티를 AuthorDto로 변환하는 전용 매퍼(정적 유틸)
// 정책: 서비스 노출명은 닉네임만 사용, profileImg는 그대로 <img src>로 사용
public final class AuthorMapper {

    private AuthorMapper() {} // 인스턴스화 금지

    public static AuthorDto from(User u) {
        if (u == null) return null; // NPE 방지
        return AuthorDto.builder()
                .id(u.getId())               // 사용자 ID
                .nick(u.getNickname())       // 닉네임
                .profileImg(u.getProfileImg()) // 프로필 이미지 데이터/URL
                .trustScore(u.getTrustScore()) // 신뢰 점수
                .build();
    }
}
