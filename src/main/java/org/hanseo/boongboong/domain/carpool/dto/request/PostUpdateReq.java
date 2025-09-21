package org.hanseo.boongboong.domain.carpool.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 카풀 게시글 수정 요청 DTO.
 * - 작성자 본인이 게시글의 핵심 정보(역할/일정/경로/좌석/메모)를 변경할 때 사용
 */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostUpdateReq {

    @NotNull private PostRole type;                 // DRIVER | RIDER
    @NotNull private LocalDate date;                // 출발 날짜
    @NotNull private LocalTime time;                // 출발 시간
    @NotBlank @Size(max = 80) private String from;  // 출발지
    @NotBlank @Size(max = 80) private String to;    // 도착지
    @Size(max = 300) private String memo;           // 메모
    private Integer seats;                          // DRIVER만 1~8, RIDER는 null
}
