// src/main/java/org/hanseo/boongboong/domain/match/service/MatchService.java
package org.hanseo.boongboong.domain.match.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.carpool.repository.PostRepo;
import org.hanseo.boongboong.domain.carpool.type.PostRole;

import org.hanseo.boongboong.domain.match.dto.MatchDtos.*;
import org.hanseo.boongboong.domain.match.entity.Match;
import org.hanseo.boongboong.domain.match.entity.MatchMember;
import org.hanseo.boongboong.domain.match.entity.MatchRequest;
import org.hanseo.boongboong.domain.match.repository.MatchMemberRepo;
import org.hanseo.boongboong.domain.match.repository.MatchRepo;
import org.hanseo.boongboong.domain.match.repository.MatchRequestRepo;
import org.hanseo.boongboong.domain.match.type.MatchStatus;
import org.hanseo.boongboong.domain.match.type.RequestStatus;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.hanseo.boongboong.domain.notify.InAppNotifyService;
import org.hanseo.boongboong.domain.notify.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MatchService {

    private final PostRepo postRepo;
    private final UserRepository userRepo;
    private final MatchRepo matchRepo;
    private final MatchRequestRepo reqRepo;
    private final MatchMemberRepo memRepo;
    
    private final InAppNotifyService notifyService;

    // ---------- 기존: 요청/승인/거절/취소 ----------

    public Long createRequest(String email, RequestCreateReq dto) {
        User me = userRepo.findByEmail(email).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        Post post = postRepo.findById(dto.postId()).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        if (post.getUser().getId().equals(me.getId()))
            throw new BusinessException(ErrorCode.INVALID_REQUEST);

        if (dto.seats() < 1) throw new BusinessException(ErrorCode.INVALID_INPUT);

        MatchRequest r = MatchRequest.builder()
                .post(post)
                .requester(me)
                .receiver(post.getUser())
                .seats(dto.seats())
                .status(RequestStatus.PENDING)
                .build();

        r = reqRepo.save(r);

        // 인앱 알림: 글 작성자(수신자)에게 새 요청
        notifyService.toUser(
                r.getReceiver().getEmail(),
                NotificationType.NEW_REQUEST,
                null,
                r.getId(),
                "새 매칭 요청이 도착했습니다."
        );
        return r.getId();
    }

    public Long approveRequest(String receiverEmail, Long requestId) {
        User receiver = userRepo.findByEmail(receiverEmail).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        MatchRequest r = reqRepo.findById(requestId).orElseThrow(() -> new BusinessException(ErrorCode.MATCH_REQUEST_NOT_FOUND));
        if (!r.isPending()) throw new BusinessException(ErrorCode.INVALID_STATE);
        if (!r.getReceiver().getId().equals(receiver.getId())) throw new BusinessException(ErrorCode.ACCESS_DENIED);

        Post post = r.getPost();

        if (post.getType() == PostRole.DRIVER) {
            Match match = matchRepo.findByDriverPostId(post.getId())
                    .orElseGet(() -> {
                        Match m = Match.fromDriverPost(post, post.getRoute().getOriginKey(), post.getRoute().getDestKey());
                        Match saved = matchRepo.save(m);
                        memRepo.save(MatchMember.driver(saved, post.getUser()));
                        return saved;
                    });

            Match locked = matchRepo.lockById(match.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            if (locked.getStatus() == MatchStatus.LOCKED) throw new BusinessException(ErrorCode.MATCH_FULL);

            try {
                locked.addSeats(r.getSeats());
            } catch (IllegalStateException e) {
                throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
            }

            User rider = r.getRequester();
            if (memRepo.existsByMatchIdAndUserId(locked.getId(), rider.getId()))
                throw new BusinessException(ErrorCode.DUPLICATE_MEMBER);

            memRepo.save(MatchMember.rider(locked, rider, r.getSeats()));
            r.approve();

            // 인앱 알림: 양측에 승인 알림
            notifyService.toUser(
                    rider.getEmail(),
                    NotificationType.REQUEST_APPROVED,
                    locked.getId(),
                    r.getId(),
                    "요청이 승인되었습니다."
            );
            notifyService.toUser(
                    locked.getDriver().getEmail(),
                    NotificationType.REQUEST_APPROVED,
                    locked.getId(),
                    r.getId(),
                    "매칭이 성사되었습니다."
            );

            // 좌석 만석 시 전체 알림(옵션)
            if (locked.getStatus() == MatchStatus.LOCKED) {
                notifyService.toMatchTopic(locked.getId(), NotificationType.MATCH_LOCKED, "좌석이 가득 찼습니다.");
            }

            return locked.getId();

        } else { // RIDER
            User driver = r.getRequester();
            if (r.getSeats() < 1) throw new BusinessException(ErrorCode.OFFER_SEATS_REQUIRED);

            Match m = Match.builder()
                    .driverPost(null)
                    .driver(driver)
                    .date(post.getDate())
                    .time(post.getTime())
                    .fromKey(post.getRoute().getOriginKey())
                    .toKey(post.getRoute().getDestKey())
                    .fromName(post.getRoute().getFrom())
                    .toName(post.getRoute().getTo())
                    .totalSeats(r.getSeats())
                    .bookedSeats(0)
                    .status(MatchStatus.OPEN)
                    .build();
            Match saved = matchRepo.save(m);

            memRepo.save(MatchMember.driver(saved, driver));
            memRepo.save(MatchMember.rider(saved, receiver, 1)); // 라이더는 항상 1명

            Match locked = matchRepo.lockById(saved.getId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.MATCH_NOT_FOUND));
            try {
                locked.addSeats(1);
            } catch (IllegalStateException e) {
                throw new BusinessException(ErrorCode.SEAT_NOT_AVAILABLE);
            }

            r.approve();

            // 인앱 알림: 양측에 승인 알림
            notifyService.toUser(
                    driver.getEmail(),
                    NotificationType.REQUEST_APPROVED,
                    locked.getId(),
                    r.getId(),
                    "요청이 승인되었습니다."
            );
            notifyService.toUser(
                    receiver.getEmail(),
                    NotificationType.REQUEST_APPROVED,
                    locked.getId(),
                    r.getId(),
                    "매칭이 성사되었습니다."
            );

            if (locked.getStatus() == MatchStatus.LOCKED) {
                notifyService.toMatchTopic(locked.getId(), NotificationType.MATCH_LOCKED, "좌석이 가득 찼습니다.");
            }

            return locked.getId();
        }
    }

    public void rejectRequest(String receiverEmail, Long requestId) {
        User me = userRepo.findByEmail(receiverEmail).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        MatchRequest r = reqRepo.findById(requestId).orElseThrow(() -> new BusinessException(ErrorCode.MATCH_REQUEST_NOT_FOUND));
        if (!r.isPending()) throw new BusinessException(ErrorCode.INVALID_STATE);
        if (!r.getReceiver().getId().equals(me.getId())) throw new BusinessException(ErrorCode.ACCESS_DENIED);
        r.reject();

        // 인앱 알림: 요청자에게 거절 알림
        notifyService.toUser(
                r.getRequester().getEmail(),
                NotificationType.REQUEST_REJECTED,
                null,
                r.getId(),
                "요청이 거절되었습니다."
        );
    }

    public void cancelRequest(String requesterEmail, Long requestId) {
        User me = userRepo.findByEmail(requesterEmail).orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        MatchRequest r = reqRepo.findById(requestId).orElseThrow(() -> new BusinessException(ErrorCode.MATCH_REQUEST_NOT_FOUND));
        if (!r.isPending()) throw new BusinessException(ErrorCode.INVALID_STATE);
        if (!r.getRequester().getId().equals(me.getId())) throw new BusinessException(ErrorCode.ACCESS_DENIED);
        r.cancel();

        // 인앱 알림: 수신자에게 취소 알림
        notifyService.toUser(
                r.getReceiver().getEmail(),
                NotificationType.REQUEST_CANCELLED,
                null,
                r.getId(),
                "요청이 취소되었습니다."
        );
    }

    // ---------- 추가: 목록/조회 ----------

    @Transactional(readOnly = true)
    public Page<MatchReqSummaryRes> getIncomingRequests(String email, RequestStatus status, Pageable pageable) {
        return reqRepo.findIncoming(email, status, pageable).map(this::toReqSummary);
    }

    @Transactional(readOnly = true)
    public Page<MatchReqSummaryRes> getSentRequests(String email, RequestStatus status, Pageable pageable) {
        return reqRepo.findSent(email, status, pageable).map(this::toReqSummary);
    }

    @Transactional(readOnly = true)
    public Page<MatchReqSummaryRes> getRequestsForPost(String email, Long postId, RequestStatus status, Pageable pageable) {
        Post post = postRepo.findById(postId).orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
        if (!post.getUser().getEmail().equals(email)) throw new BusinessException(ErrorCode.ACCESS_DENIED);
        return reqRepo.findForPost(postId, status, pageable).map(this::toReqSummary);
    }

    @Transactional(readOnly = true)
    public Page<MyMatchRes> getMyMatches(String email, PostRole role, MatchStatus status, Pageable pageable) {
        return memRepo.findMyMatches(email, role, status, pageable).map(this::toMyMatch);
    }

    @Transactional(readOnly = true)
    public java.util.List<MemberRes> getMembers(String email, Long matchId) {
        // 멤버십 확인
        if (!memRepo.existsByMatchIdAndUserEmail(matchId, email))
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        return memRepo.findByMatchId(matchId).stream().map(this::toMemberRes).toList();
    }

    @Transactional(readOnly = true)
    public MemberRes getMe(String email, Long matchId) {
        MatchMember mm = memRepo.findByMatchIdAndUserEmail(matchId, email)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACCESS_DENIED));
        return toMemberRes(mm);
    }

    @Transactional(readOnly = true)
    public CountersRes getCounters(String email) {
        long incoming = reqRepo.countByReceiverEmailAndStatus(email, RequestStatus.PENDING);
        long sent = reqRepo.countByRequesterEmailAndStatus(email, RequestStatus.PENDING);
        return new CountersRes(incoming, sent);
    }

    // ---------- mappers ----------

    private MatchReqSummaryRes toReqSummary(MatchRequest r) {
        return new MatchReqSummaryRes(
                r.getId(),
                r.getPost().getId(),
                r.getPost().getType().name(),
                new SimpleUser(r.getRequester().getId(), r.getRequester().getNickname()),
                new SimpleUser(r.getReceiver().getId(), r.getReceiver().getNickname()),
                r.getSeats(),
                r.getStatus().name(),
                r.getCreatedAt() != null ? r.getCreatedAt().toString() : null
        );
    }

    private MyMatchRes toMyMatch(MatchMember mm) {
        Match m = mm.getMatch();
        return new MyMatchRes(
                m.getId(),
                m.getDate().toString(),
                m.getTime().toString(),
                m.getFromKey(),
                m.getToKey(),
                m.getStatus().name(),
                mm.getRole().name(),
                mm.getSeats(),
                m.getTotalSeats(),
                m.getBookedSeats(),
                m.getFromName(),
                m.getToName()
        );
    }

    private MemberRes toMemberRes(MatchMember mm) {
        return new MemberRes(
                mm.getId(),
                mm.getUser().getId(),
                mm.getUser().getNickname(),
                mm.getRole().name(),
                mm.getSeats(),
                mm.getAttend().name(),
                mm.getUser().getProfileImg()
        );
    }
}
