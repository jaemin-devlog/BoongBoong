package org.hanseo.boongboong.domain.carpool.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.dto.request.PostSearchOneReq;
import org.hanseo.boongboong.domain.carpool.dto.response.PostSearchRes;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.carpool.entity.Route;
import org.hanseo.boongboong.domain.carpool.mapper.AuthorMapper;
import org.hanseo.boongboong.domain.carpool.repository.PostRepo;
import org.hanseo.boongboong.domain.carpool.type.Dir;
import org.hanseo.boongboong.domain.carpool.dto.response.PostDetailRes;
import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.hanseo.boongboong.domain.vehicle.entity.Vehicle;
import org.hanseo.boongboong.domain.vehicle.repository.VehicleRepository;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

/**
 * 카풀 게시글 조회 전용 서비스.
 * - 장소/방향/날짜/페이지 조건으로 검색 결과를 구성해 반환
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepo postRepo; // 리포지토리 의존성
    private final VehicleRepository vehicleRepository;

    public PostSearchRes searchByPlace(PostSearchOneReq req) {
        if (req.getPlace() == null || req.getPlace().isBlank())
            throw new IllegalArgumentException("place는 필수");

        String key = Route.keyOf(req.getPlace()); // 공백 제거 + 소문자 키
        String keyLike = "%" + key + "%";         // LIKE 검색 패턴
        Dir dir = req.getDir() != null ? req.getDir() : Dir.ALL; // 검색 방향 기본값
        LocalDate date = req.getDate(); // 날짜 필터 (null 가능)

        int page = req.getPage() != null ? req.getPage() : 0;   // 페이지 번호
        int size = req.getSize() != null ? req.getSize() : 20;  // 페이지 크기

        // 기본 정렬: date ASC, time ASC
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.asc("date"),
                Sort.Order.asc("time")
        ));

        // 방향 조건에 따라 다른 쿼리 실행
        Page<Post> result = switch (dir) {
            case FROM -> postRepo.findAllByOriginKeyLike(keyLike, date, req.getType(), pageable);
            case TO   -> postRepo.findAllByDestKeyLike(keyLike, date, req.getType(), pageable);
            default   -> postRepo.findAllByPlaceKeyLike(keyLike, date, req.getType(), pageable);
        };

        // 페이지 메타 + 아이템 매핑
        return PostSearchRes.builder()
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .hasNext(result.hasNext())
                .hasPrevious(result.hasPrevious())
                .items(result.map(p -> PostSearchRes.Item.builder()
                        .id(p.getId())
                        .type(p.getType())
                        .date(p.getDate())
                        .time(p.getTime())
                        .from(p.getRoute().getFrom())
                        .to(p.getRoute().getTo())
                        .seats(p.getSeats())
                        .memo(p.getMemo())
                        .dir(p.getRoute().getOriginKey().contains(key) ? "FROM" : "TO") // 매칭된 방향 표기
                        .author(AuthorMapper.from(p.getUser()))
                        .build()).getContent())
                .build();
    }

    public PostDetailRes getPostDetails(Long postId) {
        // 1. 게시글 조회 (없으면 예외)
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));

        // 2. 운전자 게시글이면, 차량 정보 조회
        Vehicle vehicle = null;
        if (post.getType() == PostRole.DRIVER) {
            // findByOwnerId는 Optional<Vehicle>을 반환
            vehicle = vehicleRepository.findByOwnerId(post.getUser().getId()).orElse(null);
        }

        // 3. DTO로 변환하여 반환
        return PostDetailRes.from(post, vehicle);
    }
}
