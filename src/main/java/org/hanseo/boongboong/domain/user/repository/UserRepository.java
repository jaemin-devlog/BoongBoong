package org.hanseo.boongboong.domain.user.repository;

import org.hanseo.boongboong.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 엔티티에 대한 데이터베이스 접근을 처리하는 리포지토리입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 자동으로 제공받습니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** 닉네임 중복 체크 */
    boolean existsByNickname(String nickname);

    /** 이메일 중복 체크 */
    boolean existsByEmail(String email);

    /** 이메일로 조회 */
    Optional<User> findByEmail(String email);

    /** username으로 조회 */
    Optional<User> findByUsername(String username);
}
