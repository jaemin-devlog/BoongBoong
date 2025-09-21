package org.hanseo.boongboong.domain.carpool.service;

import org.hanseo.boongboong.domain.carpool.dto.request.PostCreateReq;
import org.hanseo.boongboong.domain.carpool.dto.response.PostCreateRes;

public interface PostService {
    PostCreateRes create(String email, PostCreateReq req); // email만 받는다
}
