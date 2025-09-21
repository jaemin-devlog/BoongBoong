package org.hanseo.boongboong.domain.carpool.dto.response;

import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter @Builder @AllArgsConstructor @NoArgsConstructor
public class PostSearchRes {
    private int page;             // 현재 페이지(0-based)
    private int size;             // 페이지 크기
    private int totalPages;       // 총 페이지 수
    private long totalElements;   // 총 요소 수
    private boolean hasNext;      // 다음 페이지 존재
    private boolean hasPrevious;  // 이전 페이지 존재
    private List<Item> items;

    @Getter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Item {
        private Long id;
        private PostRole type;
        private LocalDate date;
        private LocalTime time;
        private String from;
        private String to;
        private Integer seats;
        private String memo;
        private String dir;        // 일반 목록은 null, 검색에서는 요청값(ALL|FROM|TO)
        private AuthorDto author;  // id, nick, profileImg
    }
}
