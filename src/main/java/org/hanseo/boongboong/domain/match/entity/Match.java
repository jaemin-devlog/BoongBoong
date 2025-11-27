package org.hanseo.boongboong.domain.match.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.match.type.MatchStatus;
import org.hanseo.boongboong.global.entity.BaseEntity;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(
        name = "match_tbl",
        indexes = {
                @Index(name = "ix_mt_dt", columnList = "date, time"),
                @Index(name = "ix_mt_status", columnList = "status")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mt_driver_post", columnNames = {"driver_post_id"})
        }
)
public class Match extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 좌석 기준 글(Driver 글) */
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "driver_post_id", nullable = true, unique = true)
    private Post driverPost;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_user_id", nullable = false)
    private User driver;

    @Column(nullable = false) private LocalDate date;
    @Column(nullable = false) private LocalTime time;

    @Column(nullable = false, length = 80) private String fromKey;
    @Column(nullable = false, length = 80) private String toKey;

    // Human-readable names captured at creation time for UI
    @Column(length = 80) private String fromName;
    @Column(length = 80) private String toName;

    @Column(nullable = false) private int totalSeats;
    @Column(nullable = false) private int bookedSeats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private MatchStatus status;

    @Version private Long ver;

    public static Match fromDriverPost(Post p, String fromKey, String toKey) {
        return Match.builder()
                .driverPost(p)
                .driver(p.getUser())
                .date(p.getDate())
                .time(p.getTime())
                .fromKey(fromKey)
                .toKey(toKey)
                .fromName(p.getRoute().getFrom())
                .toName(p.getRoute().getTo())
                .totalSeats(p.getSeats())
                .bookedSeats(0)
                .status(MatchStatus.OPEN)
                .build();
    }

    public void addSeats(int seats) {
        if (bookedSeats + seats > totalSeats) throw new IllegalStateException("SEAT_NOT_AVAILABLE");
        bookedSeats += seats;
        if (bookedSeats == totalSeats) status = MatchStatus.LOCKED;
    }

    public void releaseSeats(int seats) {
        bookedSeats = Math.max(0, bookedSeats - seats);
        if (status == MatchStatus.LOCKED && bookedSeats < totalSeats) status = MatchStatus.OPEN;
    }

    public void complete() {
        if (this.status == MatchStatus.COMPLETED) return;
        this.status = MatchStatus.COMPLETED;
    }

    public void cancelMatch() {
        if (this.status == MatchStatus.CANCELLED) return;
        this.status = MatchStatus.CANCELLED;
    }
}
