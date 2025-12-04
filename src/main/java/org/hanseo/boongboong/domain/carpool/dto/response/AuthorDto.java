package org.hanseo.boongboong.domain.carpool.dto.response;

import lombok.*;


@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthorDto {
    private Long id;
    private String nick;
    private String profileImg;
    private int trustScore;
}

