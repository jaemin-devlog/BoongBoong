/**
 * 카풀 게시글 REST 컨트롤러.
 * - 생성/단건조회/목록/수정/삭제 제공
 * - 인증 주체의 이메일은 Spring Security Authentication에서 username으로 추출
 */
package org.hanseo.boongboong.domain.carpool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.dto.request.PostCreateReq;
import org.hanseo.boongboong.domain.carpool.dto.request.PostUpdateReq;
import org.hanseo.boongboong.domain.carpool.dto.response.PostCreateRes;
import org.hanseo.boongboong.domain.carpool.dto.response.PostRes;
import org.hanseo.boongboong.domain.carpool.dto.response.PostSearchRes;
import org.hanseo.boongboong.domain.carpool.service.PostService;
import org.hanseo.boongboong.domain.carpool.dto.response.PostDetailRes;
import org.hanseo.boongboong.domain.carpool.service.PostQueryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
/**
 * 카풀 게시글 REST 컨트롤러.
 * - 생성/단건조회/목록/수정/삭제 제공
 * - 인증 주체의 이메일은 Spring Security Authentication에서 username으로 추출
 */
@RestController // REST 컨트롤러
@RequiredArgsConstructor // 생성자 주입
@RequestMapping("/api/carpool/posts") // 기본 경로
public class PostController {

    private final PostService postService; // 서비스 의존
    private final PostQueryService postQueryService;

    /** C: 생성 */
    @PostMapping
    public PostCreateRes create(
            @AuthenticationPrincipal(expression = "username") String email, // 인증 사용자 이메일
            @RequestBody @Valid PostCreateReq req                           // 생성 요청 바디
    ) {
        return postService.create(email, req); // 생성 위임
    }

    /** R: 단건 상세 조회 */
    @GetMapping("/{id}")
    public PostDetailRes get(@PathVariable Long id) { // 경로 변수로 ID
        return postQueryService.getPostDetails(id);
    }

    /** R: 목록(페이지) */
    @GetMapping
    public PostSearchRes list(
            @RequestParam(defaultValue = "0") int page, // 페이지 번호
            @RequestParam(defaultValue = "20") int size // 페이지 크기
    ) {
        Pageable pageable = PageRequest.of(page, size); // 페이지 요청 생성
        return postService.list(pageable);              // 목록 조회
    }

    /** U: 수정(작성자 본인만) */
    @PutMapping("/{id}")
    public PostRes update(
            @AuthenticationPrincipal(expression = "username") String email, // 인증 사용자 이메일
            @PathVariable Long id,                                          // 게시글 ID
            @RequestBody @Valid PostUpdateReq req                           // 수정 요청 바디
    ) {
        return postService.update(email, id, req); // 수정 위임
    }

    /** D: 삭제(하드) */
    @DeleteMapping("/{id}")
    public void delete(
            @AuthenticationPrincipal(expression = "username") String email, // 인증 사용자 이메일
            @PathVariable Long id                                          // 게시글 ID
    ) {
        postService.delete(email, id); // 삭제 위임
    }
}
