// src/main/java/org/hanseo/boongboong/domain/match/repository/MatchMemberRepo.java
package org.hanseo.boongboong.domain.match.repository;

import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.match.entity.MatchMember;
import org.hanseo.boongboong.domain.match.type.MatchStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MatchMemberRepo extends JpaRepository<MatchMember, Long> {
    boolean existsByMatchIdAndUserId(Long matchId, Long userId);

    @Query("select mm from MatchMember mm join mm.match m " +
            "where mm.user.email = :email " +
            "and (:role is null or mm.role = :role) " +
            "and (:status is null or m.status = :status)")
    Page<MatchMember> findMyMatches(@Param("email") String email,
                                    @Param("role") PostRole role,
                                    @Param("status") MatchStatus status,
                                    Pageable pageable);

    Optional<MatchMember> findByMatchIdAndUserEmail(Long matchId, String email);

    boolean existsByMatchIdAndUserEmail(Long matchId, String email);

    List<MatchMember> findByMatchId(Long matchId);
}
