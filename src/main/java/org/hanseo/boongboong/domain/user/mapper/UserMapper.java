package org.hanseo.boongboong.domain.user.mapper;

import org.hanseo.boongboong.domain.user.dto.response.SignUpRes;
import org.hanseo.boongboong.domain.user.entity.User;

/**
 * User → SignUpRes 매핑 전용 유틸.
 * - 정적 메서드만 제공
 */
public final class UserMapper {
    private UserMapper() {}

    public static SignUpRes toSignUpRes(User u) {
        if (u == null) return null;
        return SignUpRes.builder()
                .id(u.getId())
                .email(u.getEmail())
                .nickname(u.getNickname())
                .profileImg(u.getProfileImg())
                .build();
    }
}
