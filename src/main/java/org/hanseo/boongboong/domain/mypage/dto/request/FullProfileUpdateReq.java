package org.hanseo.boongboong.domain.mypage.dto.request;

public record FullProfileUpdateReq(
        String nickname,
        String name,
        Integer age,
        String profileImg,
        String intro,
        String openChatUrl
) {}
