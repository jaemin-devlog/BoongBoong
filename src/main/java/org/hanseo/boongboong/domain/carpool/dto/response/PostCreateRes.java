package org.hanseo.boongboong.domain.carpool.dto.response;

import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

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
}
