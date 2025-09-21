package org.hanseo.boongboong.domain.carpool.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 출발/도착 및 검색용 키를 보관하는 내장값(Embeddable).
 * - 입력값을 정규화하여 originKey/destKey를 생성, 장소 검색 성능/정확도 향상
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Embeddable
public class Route {

    @Column(name = "origin", nullable = false, length = 80)
    private String from; // 출발지(원문)

    @Column(name = "dest", nullable = false, length = 80)
    private String to;   // 도착지(원문)

    @Column(name = "origin_key", nullable = false, length = 100)
    private String originKey; // from을 정규화한 검색 키

    @Column(name = "dest_key", nullable = false, length = 100)
    private String destKey;   // to를 정규화한 검색 키

    /** 출발/도착 변경(키 자동 재계산) */
    public void change(String from, String to) {
        this.from = from;
        this.to = to;
        this.originKey = normalizeKey(from);
        this.destKey = normalizeKey(to);
    }

    /** 최초 생성 시 키 세팅 헬퍼 */
    public static Route of(String from, String to) {
        return Route.builder()
                .from(from)
                .to(to)
                .originKey(normalizeKey(from))
                .destKey(normalizeKey(to))
                .build();
    }

    /** 외부에서 키를 만들 때 쓰는 공개 정적 메서드(서비스/리포지토리용) */
    public static String keyOf(String s) {
        return normalizeKey(s);
    }

    /** 내부 정규화: trim → 소문자 → 다중 공백 단일화 → 공백 제거 */
    private static String normalizeKey(String s) {
        if (s == null) return "";
        return s.trim().toLowerCase()
                .replaceAll("\\s+", " ")
                .replace(" ", "");
    }
}
