// src/main/java/org/hanseo/boongboong/domain/trust/entity/TrustPointHistory.java
package org.hanseo.boongboong.domain.trust.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseo.boongboong.domain.match.entity.Match;
import org.hanseo.boongboong.domain.match.entity.MatchMember;
import org.hanseo.boongboong.domain.trust.type.TrustReason;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "trust_hist",
        indexes = {
                @Index(name = "ix_t_hist_user", columnList = "user_id"),
                @Index(name = "ix_t_hist_match", columnList = "match_id")
        }
)
public class TrustPointHistory extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 벌점 대상자 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** 관련 매칭/멤버 (증빙 추적용) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MatchMember member;

    /** 변화량: -5(당일취소), -10(노쇼) */
    @Column(nullable = false)
    private int delta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TrustReason reason;

    @Column(length = 200)
    private String memo;
}
