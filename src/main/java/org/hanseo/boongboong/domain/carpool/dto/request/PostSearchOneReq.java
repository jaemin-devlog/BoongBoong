package org.hanseo.boongboong.domain.carpool.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.Dir;

import java.time.LocalDate;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostSearchOneReq {
    @NotBlank
    private String place;          // 사용자 입력(자유 문자열)

    private Dir dir;               // FROM | TO | ALL (기본 ALL)

    private LocalDate date;        // 지정 날짜(=) 필터. null이면 today부터(>=)

    private Integer page;          // 기본 0
    private Integer size;          // 기본 20
}
