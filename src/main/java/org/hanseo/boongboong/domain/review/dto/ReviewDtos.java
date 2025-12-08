package org.hanseo.boongboong.domain.review.dto;

import jakarta.validation.constraints.*;

public class ReviewDtos {
    public record CreateReq(
            @NotNull Long matchId,
            @NotNull Long targetUserId,
            @Min(1) @Max(5) int rating,
            @Size(max = 500) String comment
    ) {}

    public record IdRes(Long id) {}

    public record CanReviewRes(
            boolean canReview,
            boolean alreadyReviewed
    ) {}
}
