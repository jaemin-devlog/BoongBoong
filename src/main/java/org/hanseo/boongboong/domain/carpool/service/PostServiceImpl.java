package org.hanseo.boongboong.domain.carpool.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.dto.request.PostCreateReq;
import org.hanseo.boongboong.domain.carpool.dto.response.PostCreateRes;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.carpool.entity.Route;
import org.hanseo.boongboong.domain.carpool.repository.PostRepo;
import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostServiceImpl implements PostService {

    private final PostRepo postRepo;
    private final UserRepository userRepo;

    @Override
    @Transactional
    public PostCreateRes create(String email, PostCreateReq req) { // ← email 기반
        // 1) 유저 조회
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 2) 날짜/시간 검증
        ensureDateTime(req.getDate(), req.getTime());

        // 3) 좌석 규칙
        normalizeSeats(req);

        // 4) 엔티티 생성
        Post post = Post.builder()
                .user(user)
                .type(req.getType())
                .date(req.getDate())
                .time(req.getTime())
                .route(Route.builder()
                        .from(req.getFrom())
                        .to(req.getTo())
                        .build())
                .seats(req.getType() == PostRole.DRIVER ? req.getSeats() : null)
                .memo(req.getMemo())
                .build();

        // 5) 도메인 보강
        post.ensureDriver();
        post.ensureRider();

        // 6) 저장
        Post saved = postRepo.save(post);

        // 7) 응답
        return PostCreateRes.builder()
                .id(saved.getId())
                .type(saved.getType())
                .date(saved.getDate())
                .time(saved.getTime())
                .from(saved.getRoute().getFrom())
                .to(saved.getRoute().getTo())
                .seats(saved.getSeats())
                .memo(saved.getMemo())
                .carNoSnap(saved.getCarNoSnap())
                .carImgSnap(saved.getCarImgSnap())
                .created(saved.getCreatedAt() != null ? saved.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private void ensureDateTime(LocalDate d, LocalTime t) {
        LocalDate today = LocalDate.now();
        if (d == null || t == null) throw new BusinessException(ErrorCode.INVALID_DATE_TIME);
        if (d.isBefore(today)) throw new BusinessException(ErrorCode.INVALID_DATE_TIME);
        if (d.isEqual(today) && t.isBefore(LocalTime.now())) throw new BusinessException(ErrorCode.INVALID_DATE_TIME);
    }

    private void normalizeSeats(PostCreateReq req) {
        if (req.getType() == PostRole.DRIVER) {
            Integer s = req.getSeats();
            if (s == null || s < 1 || s > 8)
                throw new BusinessException(ErrorCode.INVALID_SEAT_COUNT);
        } else {
            req.setSeats(null); // RIDER는 좌석 미사용
        }
    }
}
