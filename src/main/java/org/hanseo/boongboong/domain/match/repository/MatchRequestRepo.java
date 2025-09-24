// src/main/java/org/hanseo/boongboong/domain/match/repository/MatchRequestRepo.java
package org.hanseo.boongboong.domain.match.repository;

import org.hanseo.boongboong.domain.match.entity.MatchRequest;
import org.hanseo.boongboong.domain.match.type.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface MatchRequestRepo extends JpaRepository<MatchRequest, Long> {

    // 수신함(나에게 온 요청)
    @Query("select r from MatchRequest r " +
            "where r.receiver.email = :email " +
            "and (:status is null or r.status = :status)")
    Page<MatchRequest> findIncoming(@Param("email") String email,
                                    @Param("status") RequestStatus status,
                                    Pageable pageable);

    // 보낸함(내가 보낸 요청)
    @Query("select r from MatchRequest r " +
            "where r.requester.email = :email " +
            "and (:status is null or r.status = :status)")
    Page<MatchRequest> findSent(@Param("email") String email,
                                @Param("status") RequestStatus status,
                                Pageable pageable);

    // 특정 게시글로 들어온 요청
    @Query("select r from MatchRequest r " +
            "where r.post.id = :postId " +
            "and (:status is null or r.status = :status)")
    Page<MatchRequest> findForPost(@Param("postId") Long postId,
                                   @Param("status") RequestStatus status,
                                   Pageable pageable);

    long countByReceiverEmailAndStatus(String email, RequestStatus status);
    long countByRequesterEmailAndStatus(String email, RequestStatus status);
}
