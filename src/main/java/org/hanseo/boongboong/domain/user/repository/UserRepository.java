package org.hanseo.boongboong.domain.user.repository;

import org.hanseo.boongboong.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 엔티티에 대한 데이터베이스 접근을 처리하는 리포지토리입니다.
 * JpaRepository를 상속받아 기본적인 CRUD 기능을 자동으로 제공받습니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 주어진 로그인 ID가 데이터베이스에 존재하는지 확인합니다.
     * 회원가입 시 ID 중복 검사에 사용됩니다.
     */
    boolean existsByNickname(String nickname);

    /**
     * 주어진 이메일이 데이터베이스에 존재하는지 확인합니다.
     * 회원가입 시 이메일 중복 검사에 사용됩니다.
     */
    boolean existsByEmail(String email);

    /**
     * 주어진 이메일을 가진 사용자를 찾습니다.
     * 로그인 처리 등 사용자 조회가 필요할 때 사용됩니다.
     */
    Optional<User> findByEmail(String email);
}
