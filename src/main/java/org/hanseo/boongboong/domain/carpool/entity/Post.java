package org.hanseo.boongboong.domain.carpool.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.carpool.type.PostStatus;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalTime;
/**
 * 카풀 게시글 엔티티.
 * - 작성자(User), 역할/상태, 일정(날짜/시간), 경로(Route), 좌석/메모 등 보유
 * - 생성 시 상태가 비어 있으면 RECRUITING으로 초기화
 */
@Entity // JPA 엔티티
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 기본 생성자 보호
@AllArgsConstructor
@Builder
@Table(
        name = "carpool_post",
        indexes = {
                @Index(name = "idx_date_time", columnList = "date, time"),                     // 일정 검색 인덱스
                @Index(name = "idx_odate", columnList = "origin_key, dest_key, date, time")    // 출도착+일정 복합 인덱스
        }
)
public class Post extends BaseEntity { // 생성/수정시각 등 공통 필드 상속

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @ManyToOne(optional = false, fetch = FetchType.LAZY)              // 다:1 사용자
    @JoinColumn(name = "author_id", nullable = false)                 // FK 컬럼명 명시
    private User user; // 작성자

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostRole type; // DRIVER | RIDER

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostStatus status; // RECRUITING | ONGOING | COMPLETED | CANCELLED

    @Column(nullable = false)
    private LocalDate date; // 탑승 날짜

    @Column(nullable = false)
    private LocalTime time; // 탑승 시간

    @Embedded
    private Route route; // 출발/도착 및 키 내장 값

    @Column
    private Integer seats; // 운전자 글에서만 사용(탑승자 글은 null)

    @Column(length = 300)
    private String memo; // 비고

    @Column(length = 30)
    private String carNoSnap; // 차량번호 스냅샷(선택)

    private String carImgSnap; // 차량 이미지 스냅샷 URL(선택)

    @PrePersist
    public void initStatus() {         // 저장 전 상태 기본값 세팅
        if (this.status == null) {
            this.status = PostStatus.RECRUITING;
        }
    }

    // ---- 도메인 규칙 메서드 ----

    /** 생성 시 운전자 전용 규칙 보강 지점(필요 시 구현) */
    public void ensureDriver() {
        if (this.type == PostRole.DRIVER) {
            // 운전자 전용 초기화/검증 자리
        }
    }

    /** 생성 시 탑승자 전용 규칙 보강 지점(필요 시 구현) */
    public void ensureRider() {
        if (this.type == PostRole.RIDER) {
            // 탑승자 전용 초기화/검증 자리
        }
    }

    /** 전체 교체(PUT): 타입/일정/경로/좌석/메모 변경 */
    public void replaceAll(PostRole newType,
                           LocalDate newDate,
                           LocalTime newTime,
                           String newFrom,
                           String newTo,
                           Integer newSeats,
                           String newMemo) {
        this.type = newType;      // 역할 변경
        this.date = newDate;      // 날짜 변경
        this.time = newTime;      // 시간 변경
        if (this.route == null) this.route = Route.of(newFrom, newTo); // 경로 신규
        else this.route.change(newFrom, newTo);                         // 경로 변경

        this.seats = (newType == PostRole.DRIVER) ? newSeats : null; // 운전자만 좌석
        this.memo = newMemo; // 메모 교체
    }
}
