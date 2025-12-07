package org.hanseo.boongboong.domain.review.repository;

import org.hanseo.boongboong.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    boolean existsByMatchIdAndReviewerIdAndTargetId(Long matchId, Long reviewerId, Long targetId);
}

