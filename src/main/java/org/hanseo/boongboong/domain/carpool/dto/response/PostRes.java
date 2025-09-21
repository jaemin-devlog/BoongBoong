package org.hanseo.boongboong.domain.carpool.dto.response;

import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 카풀 게시글 단건 조회/목록 아이템에 사용하는 응답 DTO.
 * - 게시글 기본 정보 + 작성자 요약(AuthorDto) 포함
 */
@Getter @Builder @AllArgsConstructor @NoArgsConstructor
public class PostRes {
    private Long id;               // 게시글 ID
    private PostRole type;         // RIDER/DRIVER
    private LocalDate date;        // 출발 날짜
    private LocalTime time;        // 출발 시간
    private String from;           // 출발지
    private String to;             // 도착지
    private Integer seats;         // 좌석 수(운전자 글만)
    private String memo;           // 메모
    private String carNoSnap;      // 차량번호 스냅샷
    private String carImgSnap;     // 차량 이미지 스냅샷
    private LocalDateTime created; // 생성 시각
    private AuthorDto author;      // 작성자 요약(id, nick, profileImg)
}
