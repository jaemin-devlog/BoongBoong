package org.hanseo.boongboong.domain.carpool.dto.response;

import lombok.*;

// 역할: 작성자 정보를 API 응답으로 전달하는 가벼운 DTO (id, 닉네임, 프로필 이미지)
// 사용처: Carpool 응답 바디에서 작성자 블록에 삽입
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthorDto {
    private Long id;          // 작성자 ID
    private String nick;      // 작성자 닉네임(서비스 노출명)
    private String profileImg; // 프로필 이미지(Data URL 또는 URL)
}
