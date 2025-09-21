package org.hanseo.boongboong.domain.carpool.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.global.entity.BaseEntity;

import java.time.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "carpool_post",
        indexes = {
                @Index(name = "idx_date_time", columnList = "date, time"),
                @Index(name = "idx_odate", columnList = "origin_key, dest_key, date, time") // 출/도착 + 날짜
        }
)
public class Post extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private User user; // 작성자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PostRole type; // RIDER/DRIVER

    @Column(nullable = false)
    private LocalDate date; // 출발날짜

    @Column(nullable = false)
    private LocalTime time; // 출발시간

    @Embedded
    private Route route; // 경로(자유입력 + 키)

    @Column
    private Integer seats; // 운전자만 사용

    @Column(length = 300)
    private String memo; // 메모

    @Column(length = 30)
    private String carNoSnap; // 차량번호 스냅샷(운전자만)

    @Column(length = 255)
    private String carImgSnap; // 차량이미지 스냅샷(운전자만)

    @Builder.Default
    private boolean del = false; // 소프트삭제

    @PrePersist @PreUpdate
    private void prePersistUpdate() { // 저장/수정 전에 키 동기화
        if (route != null) route.syncKeys();
    }

    // --- 도메인 규칙 ---
    public void ensureDriver() {
        if (type != PostRole.DRIVER) return;
        if (seats == null || seats < 1 || seats > 8) {
            throw new IllegalArgumentException("좌석수는 1~8 범위여야 함");
        }
    }

    public void ensureRider() {
        if (type == PostRole.RIDER) this.seats = null; // 탑승자는 좌석 미사용
    }

    public void delete() { this.del = true; } // 소프트삭제
}
