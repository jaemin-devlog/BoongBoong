package org.hanseo.boongboong.domain.carpool.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.dto.request.PostCreateReq;
import org.hanseo.boongboong.domain.carpool.dto.request.PostUpdateReq;
import org.hanseo.boongboong.domain.carpool.dto.response.PostCreateRes;
import org.hanseo.boongboong.domain.carpool.dto.response.PostRes;
import org.hanseo.boongboong.domain.carpool.dto.response.PostSearchRes;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.carpool.entity.Route;
import org.hanseo.boongboong.domain.carpool.mapper.AuthorMapper;
import org.hanseo.boongboong.domain.carpool.mapper.PostMapper;
import org.hanseo.boongboong.domain.carpool.repository.PostRepo;
import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

/**
 * 카풀 게시글 도메인 서비스 구현.
 * - 생성 시 유효성 검증/도메인 규칙 보강, 수정/삭제 시 권한 확인 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 기본은 조회 트랜잭션(쓰기 메서드에만 @Transactional 재지정)
public class PostServiceImpl implements PostService {

    private final PostRepo postRepo;       // Post JPA 리포지토리
    private final UserRepository userRepo; // User JPA 리포지토리

    /**
     * 게시글 생성 유스케이스.
     * 1) 작성자 조회 → 2) 날짜/시간 검증 → 3) 좌석 검증 → 4) 엔티티 조립 → 5) 규칙 보강 → 6) 저장 → 7) 응답 매핑
     */
    @Override
    @Transactional // 쓰기 트랜잭션
    public PostCreateRes create(String email, PostCreateReq req) {
        User user = findUserByEmail(email);                  // 1) 작성자 단건 조회(없으면 예외)
        validateDateTime(req.getDate(), req.getTime());      // 2) 출발 날짜/시간 유효성 검사(과거 금지)
        validateSeats(req.getType(), req.getSeats());        // 3) 좌석 수 범위/필수 검사(운전자만)

        // 4) 엔티티 조립: 요청값을 기반으로 Post 빌더 구성
        Post post = Post.builder()
                .user(user)                                  // 작성자
                .type(req.getType())                         // 역할(DRIVER/RIDER)
                .date(req.getDate())                         // 출발 날짜
                .time(req.getTime())                         // 출발 시간
                .route(Route.of(req.getFrom(), req.getTo())) // 경로(출발/도착 + 키)
                .seats(isDriver(req.getType()) ? req.getSeats() : null) // 운전자 글만 좌석 보유
                .memo(req.getMemo())                         // 메모
                .build();

        // 5) 역할별 도메인 규칙 보강(추후 제약 추가 지점)
        post.ensureDriver();
        post.ensureRider();

        // 6) 저장
        Post saved = postRepo.save(post);

        // 7) 응답 매핑 DTO(PostCreateRes)로 변환 후 반환
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
                .author(AuthorMapper.from(saved.getUser()))
                .build();
    }

    /**
     * 게시글 단건 조회.
     * - ID로 Post를 찾고 응답 DTO로 매핑
     */
    @Override
    public PostRes get(Long id) {
        Post p = findPost(id);    // 존재 검증 포함
        return PostMapper.toRes(p); // 엔티티 → 응답 DTO
    }

    /**
     * 게시글 목록 페이지 조회.
     * - Pageable로 페이징/정렬 수용
     */
    @Override
    public PostSearchRes list(Pageable pageable) {
        Page<Post> page = postRepo.findAll(pageable); // 페이지 쿼리
        // 페이지 메타데이터 + 아이템 매핑 조립
        return PostSearchRes.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .items(page.map(p -> PostSearchRes.Item.builder()
                        .id(p.getId())
                        .type(p.getType())
                        .date(p.getDate())
                        .time(p.getTime())
                        .from(p.getRoute().getFrom())
                        .to(p.getRoute().getTo())
                        .seats(p.getSeats())
                        .memo(p.getMemo())
                        .dir(null) // 장소 기반 검색이 아니므로 방향 정보는 비워둠
                        .author(AuthorMapper.from(p.getUser()))
                        .build()).getContent())
                .build();
    }

    /**
     * 게시글 수정 유스케이스.
     * 1) 대상 조회 → 2) 권한 확인(작성자 본인) → 3) 유효성 검사 → 4) 값 교체 → 5) 규칙 보강 → 6) 응답 매핑
     * - JPA Dirty Checking으로 트랜잭션 종료 시점에 UPDATE flush됨
     */
    @Override
    @Transactional // 쓰기 트랜잭션
    public PostRes update(String email, Long id, PostUpdateReq req) {
        Post p = findPost(id);                  // 1) 대상 조회(없으면 예외)
        ensureAuthor(p, email);                 // 2) 작성자 권한 확인(불일치 시 ACCESS_DENIED)

        validateDateTime(req.getDate(), req.getTime()); // 3) 일정 유효성
        validateSeats(req.getType(), req.getSeats());   // 3) 좌석 유효성(운전자만)

        // 4) 필드 교체(도메인 메서드로 일괄 변경)
        p.replaceAll(
                req.getType(),
                req.getDate(),
                req.getTime(),
                req.getFrom(),
                req.getTo(),
                req.getSeats(),
                req.getMemo()
        );

        // 5) 역할별 규칙 보강(추후 제약 추가 지점)
        p.ensureDriver();
        p.ensureRider();

        // 6) 응답 매핑: 영속 상태 엔티티를 그대로 DTO로 변환
        return PostMapper.toRes(p);
    }

    /**
     * 게시글 하드 삭제.
     * - 작성자 본인 확인 후 delete 수행
     */
    @Override
    @Transactional // 쓰기 트랜잭션
    public void delete(String email, Long id) {
        Post p = findPost(id);     // 대상 조회
        ensureAuthor(p, email);    // 권한 확인
        postRepo.delete(p);        // 하드 삭제
    }

    // ===== 내부 유틸 =====

    /**
     * 이메일로 사용자 단건 조회(없으면 USER_NOT_FOUND).
     */
    private User findUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * ID로 게시글 단건 조회(없으면 NOT_FOUND).
     */
    private Post findPost(Long id) {
        return postRepo.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND));
    }

    /**
     * 작성자 본인 여부 확인(불일치 시 ACCESS_DENIED).
     */
    private void ensureAuthor(Post p, String email) {
        if (!p.getUser().getEmail().equals(email)) { // 이메일 동일성 비교
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }
    }

    /**
     * 출발 날짜/시간 유효성 검사.
     * - null 금지
     * - 과거 날짜/시간 금지(오늘 날짜면 현재 시각 이전 금지)
     */
    private void validateDateTime(LocalDate d, LocalTime t) {
        LocalDate today = LocalDate.now();
        if (d == null || t == null) throw new BusinessException(ErrorCode.INVALID_DATE_TIME);
        if (d.isBefore(today)) throw new BusinessException(ErrorCode.INVALID_DATE_TIME);
        if (d.isEqual(today) && t.isBefore(LocalTime.now())) throw new BusinessException(ErrorCode.INVALID_DATE_TIME);
    }

    /**
     * 좌석 수 유효성 검사(운전자 글만 적용).
     * - null/범위(1~8) 위반 시 INVALID_SEAT_COUNT
     */
    private void validateSeats(PostRole type, Integer seats) {
        if (isDriver(type)) {
            if (seats == null || seats < 1 || seats > 8) {
                throw new BusinessException(ErrorCode.INVALID_SEAT_COUNT);
            }
        }
    }

    /**
     * DRIVER 글 여부 헬퍼.
     */
    private boolean isDriver(PostRole type) { return type == PostRole.DRIVER; }
}
