// src/main/java/org/hanseo/boongboong/domain/match/entity/MatchRequest.java
package org.hanseo.boongboong.domain.match.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.match.type.RequestStatus;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "match_req",
        indexes = {
                @Index(name = "ix_req_status", columnList = "status"),
                @Index(name = "ix_req_post", columnList = "post_id")
        })
public class MatchRequest extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 신청을 건 '단일' 대상 게시글 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // target post

    /** 신청자 / 수신자(게시글 작성자) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requester_id")
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "receiver_id")
    private User receiver; // post.getUser()

    /**
     * 좌석 관련
     * - 타겟 글이 DRIVER면: rider가 요청한 좌석 수 (보통 1)
     * - 타겟 글이 RIDER면: driver가 제안하는 총 좌석 수(필수)
     */
    @Column(nullable = false)
    private int seats; // 의미는 위 설명대로, 문맥에 따라 다르게 쓰임

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RequestStatus status; // PENDING/APPROVED/REJECTED/CANCELLED

    private LocalDateTime respondedAt;
    private LocalDateTime cancelledAt;

    public void approve() { this.status = RequestStatus.APPROVED; this.respondedAt = LocalDateTime.now(); }
    public void reject()  { this.status = RequestStatus.REJECTED; this.respondedAt = LocalDateTime.now(); }
    public void cancel()  { this.status = RequestStatus.CANCELLED; this.cancelledAt = LocalDateTime.now(); }
    public boolean isPending() { return this.status == RequestStatus.PENDING; }
}
