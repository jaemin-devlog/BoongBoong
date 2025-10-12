package org.hanseo.boongboong.domain.carpool.repository;

import org.hanseo.boongboong.domain.carpool.entity.Post;
import org.hanseo.boongboong.domain.carpool.type.PostRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
/**
 * 카풀 게시글 리포지토리.
 * - 장소 키 기반 페이지 검색, 마이페이지용 목록/다가올 일정 조회 제공
 */
public interface PostRepo extends JpaRepository<Post, Long> {

    // ===== 검색 =====
    @Query("""
           select p
             from Post p
            where (:date is null or p.date = :date)
              and p.route.originKey like :keyLike
              and (:type is null or p.type = :type)
         order by p.date asc, p.time asc
           """)
    Page<Post> findAllByOriginKeyLike(@Param("keyLike") String keyLike,
                                      @Param("date") LocalDate date,
                                      @Param("type") PostRole type,
                                      Pageable pageable);

    @Query("""
           select p
             from Post p
            where (:date is null or p.date = :date)
              and p.route.destKey like :keyLike
              and (:type is null or p.type = :type)
         order by p.date asc, p.time asc
           """)
    Page<Post> findAllByDestKeyLike(@Param("keyLike") String keyLike,
                                    @Param("date") LocalDate date,
                                    @Param("type") PostRole type,
                                    Pageable pageable);

    @Query("""
           select p
             from Post p
            where (:date is null or p.date = :date)
              and (p.route.originKey like :keyLike or p.route.destKey like :keyLike)
              and (:type is null or p.type = :type)
         order by p.date asc, p.time asc
           """)
    Page<Post> findAllByPlaceKeyLike(@Param("keyLike") String keyLike,
                                     @Param("date") LocalDate date,
                                     @Param("type") PostRole type,
                                     Pageable pageable);

    // ===== MyPage용 =====
    List<Post> findTop50ByUserIdOrderByCreatedAtDesc(Long userId); // 내가 올린 최신 50

    /** 가장 가까운 '다가올' 카풀 후보들을 정렬해서 모두 반환 (서비스에서 첫 1개만 선택) */
    @Query("""
           select p
             from Post p
            where p.user.id = :userId
              and (p.date > :today or (p.date = :today and p.time >= :now))
         order by p.date asc, p.time asc
           """)
    List<Post> findNearestUpcomingByAuthor(@Param("userId") Long userId,
                                           @Param("today") LocalDate today,
                                           @Param("now") LocalTime now);

    /** '다가올' 카풀 전체 리스트 */
    @Query("""
           select p
             from Post p
            where p.user.id = :userId
              and (p.date > :today or (p.date = :today and p.time >= :now))
         order by p.date asc, p.time asc
           """)
    List<Post> findAllUpcomingByAuthor(@Param("userId") Long userId,
                                                                               @Param("today") LocalDate today,
                                                                               @Param("now") LocalTime now);
                                       
                                           /** '완료된' 카풀 전체 리스트 */
                                           @Query("""
                                                  select p
                                                    from Post p
                                                   where p.user.id = :userId
                                                     and (p.date < :today or (p.date = :today and p.time < :now))
                                                order by p.date desc, p.time desc
                                                  """)
                                           List<Post> findAllCompletedByAuthor(@Param("userId") Long userId,
                                                                               @Param("today") LocalDate today,
                                                                               @Param("now") LocalTime now);
                                       }
