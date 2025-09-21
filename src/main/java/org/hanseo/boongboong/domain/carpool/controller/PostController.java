package org.hanseo.boongboong.domain.carpool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.dto.request.PostCreateReq;
import org.hanseo.boongboong.domain.carpool.dto.response.PostCreateRes;
import org.hanseo.boongboong.domain.carpool.service.PostService;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carpool/posts")
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostCreateRes> create(
            @Valid @RequestBody PostCreateReq req,
            Authentication auth
    ) {
        String email = auth.getName();                 // 이메일 추출
        PostCreateRes res = postService.create(email, req); //유저 조회검증 담당
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }
}
