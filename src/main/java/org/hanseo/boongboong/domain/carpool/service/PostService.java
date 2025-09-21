package org.hanseo.boongboong.domain.carpool.service;

import org.hanseo.boongboong.domain.carpool.dto.request.PostCreateReq;
import org.hanseo.boongboong.domain.carpool.dto.request.PostUpdateReq;
import org.hanseo.boongboong.domain.carpool.dto.response.PostCreateRes;
import org.hanseo.boongboong.domain.carpool.dto.response.PostRes;
import org.hanseo.boongboong.domain.carpool.dto.response.PostSearchRes;
import org.springframework.data.domain.Pageable;

/**
 * 카풀 게시글 도메인 서비스 인터페이스.
 * - 생성/조회/목록/수정/삭제 핵심 유스케이스 정의
 */
public interface PostService {
    PostCreateRes create(String email, PostCreateReq req); // 생성
    PostRes get(Long id);                                  // 단건 조회
    PostSearchRes list(Pageable pageable);                  // 목록 조회(페이지)
    PostRes update(String email, Long id, PostUpdateReq req); // 수정(작성자만)
    void delete(String email, Long id);                    // 하드 삭제
}
