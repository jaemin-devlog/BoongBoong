// src/main/java/org/hanseo/boongboong/domain/match/repository/MatchRepo.java
package org.hanseo.boongboong.domain.match.repository;

import jakarta.persistence.LockModeType;
import org.hanseo.boongboong.domain.match.entity.Match;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MatchRepo extends JpaRepository<Match, Long> {
    Optional<Match> findByDriverPostId(Long driverPostId);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("select m from Match m where m.id = :id")
    Optional<Match> lockById(@Param("id") Long id);
}
