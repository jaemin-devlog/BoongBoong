package org.hanseo.boongboong.domain.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.review.dto.ReviewDtos.*;
import org.hanseo.boongboong.domain.review.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<IdRes> create(
            @AuthenticationPrincipal(expression = "username") String email,
            @Valid @RequestBody CreateReq req
    ) {
        Long id = reviewService.create(email, req);
        return ResponseEntity.ok(new IdRes(id));
    }
}

