package org.hanseo.boongboong.domain.carpool.repository;

import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;

public interface PostRepo extends JpaRepository<Post, Long> {

    // ALL: 출발/도착 둘 중 하나라도 포함 매칭, 출발 우선 정렬
    @Query("""
        select p from Post p
         where p.del = false
           and p.date >= :fromDate
           and (p.route.fromKey like :keyLike or p.route.toKey like :keyLike)
         order by
           case when p.route.fromKey like :keyLike then 0 else 1 end,
           p.date asc, p.time asc
        """)
    Page<Post> findAllByPlaceKeyFromLike(String keyLike, LocalDate fromDate, Pageable pageable);

    // FROM 전용
    @Query("""
        select p from Post p
         where p.del = false
           and p.date >= :fromDate
           and p.route.fromKey like :keyLike
         order by p.date asc, p.time asc
        """)
    Page<Post> findAllByFromKeyFromLike(String keyLike, LocalDate fromDate, Pageable pageable);

    // TO 전용
    @Query("""
        select p from Post p
         where p.del = false
           and p.date >= :fromDate
           and p.route.toKey like :keyLike
         order by p.date asc, p.time asc
        """)
    Page<Post> findAllByToKeyFromLike(String keyLike, LocalDate fromDate, Pageable pageable);
}
