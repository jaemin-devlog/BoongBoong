package org.hanseo.boongboong.domain.carpool.dto.response;

import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PostSearchRes {
    private long total;      // 전체 건수(필터 반영)
    private long totalFrom;  // 출발 매칭 수
    private long totalTo;    // 도착 매칭 수
    private List<Item> items;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Item {
        private Long id;
        private PostRole type;
        private LocalDate date;
        private LocalTime time;
        private String from;
        private String to;
        private Integer seats;
        private String memo;
        private String dir; // "FROM" or "TO"
    }
}
