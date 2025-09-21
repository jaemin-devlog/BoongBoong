package org.hanseo.boongboong.domain.carpool.dto.response;

import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
/**
 * 카풀 게시글 생성 응답 DTO.
 * - 생성 직후 클라이언트에 돌려줄 게시글 기본 정보와 작성자 정보 포함
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRes {
    private Long id;                 // 글 ID
    private PostRole type;           // RIDER/DRIVER
    private LocalDate date;          // 출발날짜
    private LocalTime time;          // 출발시간
    private String from;             // 출발지(문자열)
    private String to;               // 도착지(문자열)
    private Integer seats;           // 좌석(운전자만)
    private String memo;             // 메모
    private String carNoSnap;        // 차량번호 스냅샷
    private String carImgSnap;       // 차량이미지 스냅샷
    private LocalDateTime created;   // 생성시각
    private AuthorDto author;        // 작성자 요약 정보
}
