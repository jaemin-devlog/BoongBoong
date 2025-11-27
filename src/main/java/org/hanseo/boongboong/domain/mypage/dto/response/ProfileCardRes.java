package org.hanseo.boongboong.domain.mypage.dto.response;

import org.hanseo.boongboong.domain.user.entity.Role;

public record ProfileCardRes(
        String email,
        String nickname,
        String profileImg,
        Role role,
        int trustScore,
        boolean hasDriverLicense,
        String intro,
        String openChatUrl,
        VehicleInfoRes vehicleInfo
) {
    public record VehicleInfoRes(
            String number,
            String imageUrl,
            Integer seats,
            String color
    ) { }
}

