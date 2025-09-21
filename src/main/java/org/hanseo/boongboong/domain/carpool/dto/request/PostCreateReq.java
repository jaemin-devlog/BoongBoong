package org.hanseo.boongboong.domain.carpool.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PostCreateReq {
    @NotNull private PostRole type;           // RIDER/DRIVER
    @NotNull private LocalDate date;          // 출발날짜
    @NotNull private LocalTime time;          // 출발시간
    @NotBlank @Size(max = 80)  private String from;  // 출발지(자유입력)
    @NotBlank @Size(max = 80)  private String to;    // 도착지(자유입력)
    @Min(1) @Max(8) private Integer seats;    // 운전자만 사용
    @Size(max = 300) private String memo;     // 메모
}
