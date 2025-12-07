package org.hanseo.boongboong.domain.review.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.match.entity.Match;
import org.hanseo.boongboong.domain.match.repository.MatchMemberRepo;
import org.hanseo.boongboong.domain.match.repository.MatchRepo;
import org.hanseo.boongboong.domain.match.type.MatchStatus;
import org.hanseo.boongboong.domain.review.dto.ReviewDtos.*;
import org.hanseo.boongboong.domain.review.entity.Review;
import org.hanseo.boongboong.domain.review.repository.ReviewRepository;
import org.hanseo.boongboong.domain.trust.entity.TrustPointHistory;
import org.hanseo.boongboong.domain.trust.repository.TrustPointHistoryRepo;
import org.hanseo.boongboong.domain.trust.type.TrustReason;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final MatchRepo matchRepo;
    private final MatchMemberRepo matchMemberRepo;
    private final UserRepository userRepo;
    private final TrustPointHistoryRepo trustRepo;

    private static final int REVIEW_BONUS = 5;

    public Long create(String reviewerEmail, CreateReq req) {
        User reviewer = userRepo.findByEmail(reviewerEmail)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Match match = matchRepo.findById(req.matchId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
        if (match.getStatus() != MatchStatus.COMPLETED) {
            throw new BusinessException(ErrorCode.INVALID_STATE);
        }
        User target = userRepo.findById(req.targetUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 둘 다 해당 매치의 멤버인지 검증
        if (!matchMemberRepo.existsByMatchIdAndUserId(match.getId(), reviewer.getId()) ||
            !matchMemberRepo.existsByMatchIdAndUserId(match.getId(), target.getId())) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 중복 리뷰 방지
        if (reviewRepo.existsByMatchIdAndReviewerIdAndTargetId(match.getId(), reviewer.getId(), target.getId())) {
            throw new BusinessException(ErrorCode.INVALID_STATE);
        }

        Review saved = reviewRepo.save(Review.builder()
                .match(match)
                .reviewer(reviewer)
                .target(target)
                .rating(req.rating())
                .comment(req.comment())
                .build());

        // 신뢰 점수 가점 기록 및 사용자 점수 반영
        target.updateProfileBasics(null, null, null, null, null); // touch to mark dirty if needed
        target.setTrustScore(target.getTrustScore() + REVIEW_BONUS);
        userRepo.save(target);

        trustRepo.save(TrustPointHistory.builder()
                .user(target)
                .match(match)
                .member(null)
                .delta(REVIEW_BONUS)
                .reason(TrustReason.REVIEW)
                .memo("리뷰 가점")
                .build());

        return saved.getId();
    }
}

