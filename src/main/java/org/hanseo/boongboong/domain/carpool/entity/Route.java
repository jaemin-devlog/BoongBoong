package org.hanseo.boongboong.domain.carpool.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Embeddable
public class Route { // 경로(자유입력)

    @Column(name = "origin", nullable = false, length = 80) // 출발지(표시용)
    private String from; // 출발지

    @Column(name = "dest", nullable = false, length = 80)   // 도착지(표시용)
    private String to;   // 도착지

    @Column(name = "origin_key", nullable = false, length = 100) // 출발지 키
    private String fromKey; // 공백 제거 + 소문자

    @Column(name = "dest_key", nullable = false, length = 100)   // 도착지 키
    private String toKey;   // 공백 제거 + 소문자

    // ===== 정규화 유틸 =====
    public static String keyOf(String s) {
        if (s == null) return null;
        String t = s.replaceAll("\\s+", ""); // 모든 공백 제거
        return t.toLowerCase();              // 소문자
    }

    public void syncKeys() { // from/to → key 동기화
        this.fromKey = keyOf(this.from);
        this.toKey   = keyOf(this.to);
    }

    // 선택적 세터(엔티티 변경 시 키 자동 반영)
    public void setFrom(String from) { this.from = from; this.fromKey = keyOf(from); }
    public void setTo(String to)     { this.to   = to;   this.toKey   = keyOf(to); }
}
