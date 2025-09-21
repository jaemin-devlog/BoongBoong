package org.hanseo.boongboong.domain.carpool.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.dto.request.PostSearchOneReq;
import org.hanseo.boongboong.domain.carpool.dto.response.PostSearchRes;
import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.carpool.entity.Route;
import org.hanseo.boongboong.domain.carpool.repository.PostRepo;
import org.hanseo.boongboong.domain.carpool.type.Dir;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepo postRepo;

    public PostSearchRes searchByPlace(PostSearchOneReq req) {
        if (req.getPlace() == null || req.getPlace().isBlank())
            throw new IllegalArgumentException("place는 필수");

        String key = Route.keyOf(req.getPlace());     // 공백 제거 + 소문자
        String keyLike = "%" + key + "%";             // 부분 일치
        Dir dir = req.getDir() != null ? req.getDir() : Dir.ALL;

        LocalDate base = (req.getDate() != null) ? req.getDate() : LocalDate.now(); // 항상 >= 기준

        Pageable pg = PageRequest.of(
                req.getPage() != null ? req.getPage() : 0,
                req.getSize() != null ? req.getSize() : 20
        );

        Page<Post> page = switch (dir) {
            case FROM -> postRepo.findAllByFromKeyFromLike(keyLike, base, pg);
            case TO   -> postRepo.findAllByToKeyFromLike(keyLike, base, pg);
            default   -> postRepo.findAllByPlaceKeyFromLike(keyLike, base, pg);
        };

        return PostSearchRes.builder()
                .total(page.getTotalElements())
                .items(page.map(p -> PostSearchRes.Item.builder()
                        .id(p.getId())
                        .type(p.getType())
                        .date(p.getDate())
                        .time(p.getTime())
                        .from(p.getRoute().getFrom())
                        .to(p.getRoute().getTo())
                        .seats(p.getSeats())
                        .memo(p.getMemo())
                        .dir(p.getRoute().getFromKey().contains(key) ? "FROM" : "TO")
                        .build()).getContent())
                .build();
    }
}
