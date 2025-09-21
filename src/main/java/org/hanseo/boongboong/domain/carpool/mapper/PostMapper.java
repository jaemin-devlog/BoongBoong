package org.hanseo.boongboong.domain.carpool.mapper;

import org.hanseo.boongboong.domain.carpool.dto.response.PostRes;
import org.hanseo.boongboong.domain.carpool.entity.Post;

import java.time.LocalDateTime;
/**
 * Post 엔티티 → API 응답(PostRes) 매핑 유틸.
 * - 정적 메서드로만 구성된 매퍼 클래스
 */
public final class PostMapper {
    private PostMapper() {} // 인스턴스화 방지

    public static PostRes toRes(Post p) {
        if (p == null) return null; // NPE 방지
        return PostRes.builder()
                .id(p.getId())                              // 게시글 ID
                .type(p.getType())                          // DRIVER/RIDER
                .date(p.getDate())                          // 날짜
                .time(p.getTime())                          // 시간
                .from(p.getRoute().getFrom())               // 출발지
                .to(p.getRoute().getTo())                   // 도착지
                .seats(p.getSeats())                        // 좌석(운전자만)
                .memo(p.getMemo())                          // 메모
                .carNoSnap(p.getCarNoSnap())                // 차량번호 스냅샷
                .carImgSnap(p.getCarImgSnap())              // 차량이미지 스냅샷
                .created(p.getCreatedAt() != null ? p.getCreatedAt() : LocalDateTime.now()) // 생성시각
                .author(AuthorMapper.from(p.getUser()))     // 작성자 정보
                .build();
    }
}
