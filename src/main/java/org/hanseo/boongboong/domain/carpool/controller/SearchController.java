// CarpoolSearchController.java
package org.hanseo.boongboong.domain.carpool.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.carpool.dto.request.PostSearchOneReq;
import org.hanseo.boongboong.domain.carpool.dto.response.PostSearchRes;
import org.hanseo.boongboong.domain.carpool.service.PostQueryService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carpool/posts")
public class SearchController {

    private final PostQueryService queryService; // 검색

    @GetMapping("/search")
    public PostSearchRes searchByPlace(@Valid PostSearchOneReq req) {
        return queryService.searchByPlace(req);
    }
}
