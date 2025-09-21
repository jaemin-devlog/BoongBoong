package org.hanseo.boongboong.domain.user.repository;

import org.hanseo.boongboong.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 엔티티 리포지토리.
 * - 이메일/닉네임 중복 체크, 이메일 단건 조회 제공
 */
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);                  // 이메일 중복 여부
    boolean existsByNickname(String nickname);            // 닉네임 중복 여부
    Optional<User> findByEmail(String email);             // 이메일로 조회
    boolean existsByNicknameAndEmailNot(String nickname, String email); // 내 이메일 제외 닉네임 중복
}
