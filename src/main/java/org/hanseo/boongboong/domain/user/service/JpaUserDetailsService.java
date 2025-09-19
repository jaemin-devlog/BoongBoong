// domain/user/service/JpaUserDetailsService.java
package org.hanseo.boongboong.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service("jpaUserDetailsService")
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository; //DB에서 User 조회

    /**
     * 스프링 시큐리티에서 로그인 시 호출
     * username 파라미터로 이메일을 받아 DB에서 User 조회 후 UserDetails 객체로 변환
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username(email)으로 DB에서 사용자 검색, 없으면 예외 발생
        User u = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage()));
        // Role Enum(USER, DRIVER, ADMIN)을 스프링 시큐리티 규칙에 맞게 "ROLE_..." 형식으로 변환
        String roleName = "ROLE_" + u.getRole().name();

        // 스프링 시큐리티 UserDetails 객체 생성
        // 첫 번째 인자: 로그인 ID(email)
        // 두 번째 인자: 암호화된 비밀번호
        // 세 번째 인자: 권한 목록 (여기서는 단일 권한만 부여)
        return new org.springframework.security.core.userdetails.User(
                u.getEmail(),
                u.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(roleName))
        );
    }
}