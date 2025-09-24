// src/main/java/org/hanseo/boongboong/domain/match/entity/MatchMember.java
package org.hanseo.boongboong.domain.match.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.match.type.AttendanceStatus;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.global.entity.BaseEntity;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "match_mem",
        uniqueConstraints = @UniqueConstraint(name = "uk_match_mem", columnNames = {"match_id","user_id"}),
        indexes = @Index(name = "ix_mem_match", columnList = "match_id"))
public class MatchMember extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "match_id")
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 8)
    private PostRole role; // DRIVER/RIDER

    @Column(nullable = false) private int seats;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10)
    private AttendanceStatus attend; // UNKNOWN/ATTENDED/NO_SHOW/CANCELLED

    private LocalDateTime joinedAt;
    private LocalDateTime cancelledAt;

    public static MatchMember driver(Match m, User u) {
        return MatchMember.builder()
                .match(m).user(u).role(PostRole.DRIVER)
                .seats(0).attend(AttendanceStatus.UNKNOWN)
                .joinedAt(LocalDateTime.now()).build();
    }
    public static MatchMember rider(Match m, User u, int seats) {
        return MatchMember.builder()
                .match(m).user(u).role(PostRole.RIDER)
                .seats(seats).attend(AttendanceStatus.UNKNOWN)
                .joinedAt(LocalDateTime.now()).build();
    }
    public void setAttend(AttendanceStatus s){ this.attend = s; this.cancelledAt = (s==AttendanceStatus.CANCELLED)? java.time.LocalDateTime.now(): this.cancelledAt; }

}
