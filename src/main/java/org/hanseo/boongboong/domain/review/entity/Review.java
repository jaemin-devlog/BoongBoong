package org.hanseo.boongboong.domain.review.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseo.boongboong.domain.match.entity.Match;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.global.entity.BaseEntity;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "review",
        uniqueConstraints = @UniqueConstraint(name = "uk_review_match_reviewer_target", columnNames = {"match_id","reviewer_id","target_id"}),
        indexes = {
                @Index(name = "ix_review_match", columnList = "match_id"),
                @Index(name = "ix_review_target", columnList = "target_id")
        }
)
public class Review extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "target_id", nullable = false)
    private User target;

    @Column(nullable = false)
    private int rating; // 1~5

    @Column(length = 500)
    private String comment;
}

