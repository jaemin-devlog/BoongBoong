package org.hanseo.boongboong.domain.mypage.dto.response;

import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalTime;

public record CompletedCarpoolRes(
        Long matchId,
        Long postId,
        PostRole myRole,
        String origin,
        String destination,
        LocalDate date,
        LocalTime time,
        String profileImageUrl,
        String name,
        String nickname,
        String memo,
        Integer trustPoint
) {}