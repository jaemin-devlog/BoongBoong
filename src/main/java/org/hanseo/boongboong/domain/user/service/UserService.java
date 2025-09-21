package org.hanseo.boongboong.domain.user.service; // 패키지 선언

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hanseo.boongboong.domain.user.dto.request.SignUpRequestDto;
import org.hanseo.boongboong.domain.user.entity.Role;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.domain.user.repository.UserRepository;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.hanseo.boongboong.global.util.EmailDomainValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 계정 도메인 서비스.
 * - 회원가입, 닉네임 변경, 비밀번호 재설정, 중복 체크 등 계정 관련 핵심 유즈케이스 구현
 */
@Slf4j // Slf4j 로거
@Service
@RequiredArgsConstructor // 의존성 주입용 생성자 자동 생성
@Transactional(readOnly = true) // 기본 읽기 트랜잭션
public class UserService {

    private final UserRepository userRepository;         // User JPA 리포지토리
    private final EmailService emailService;             // 이메일 인증 서비스
    private final EmailDomainValidator emailDomainValidator; // 학교 도메인 검증기
    private final PasswordEncoder passwordEncoder;       // 비밀번호 인코더

    // 가입/닉네임 변경 시 아바타 확정용 (닉네임 기반 이니셜/색상, 대비 자동 전환, SVG Data URL)
    private final AvatarGenerator avatarGenerator;

    private static final int DEFAULT_TRUST_SCORE = 50;   // 기본 신뢰 점수

    /**
     * 회원가입.
     * 1) 이메일 도메인/인증/중복, 닉네임 중복 검증
     * 2) 비밀번호 해싱
     * 3) User 엔티티 생성/저장
     * 4) 아바타 생성 적용 후 재저장
     * 5) 인증 토큰 소거
     * 6) 생성된 사용자 ID 반환
     */
    @Transactional
    public Long signup(SignUpRequestDto dto) { // 회원가입
        // 1-1) 허용 도메인인지 검사(학교 이메일)
        if (!emailDomainValidator.isAllowed(dto.email())) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_DOMAIN);
        }
        // 1-2) 이메일 인증 완료 여부 확인
        if (!emailService.isVerified(dto.email())) {
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        // 1-3) 이메일 중복 검사
        if (userRepository.existsByEmail(dto.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        // 1-4) 닉네임 중복 검사
        if (userRepository.existsByNickname(dto.nickname())) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 2) 비밀번호 해싱
        String encodedPw = passwordEncoder.encode(dto.password());

        // 3) User 엔티티 조립 및 1차 저장(식별자 발급 목적)
        User user = User.builder()
                .email(dto.email())                         // 이메일(=로그인 아이디)
                .password(encodedPw)                        // 해시된 비밀번호
                .nickname(dto.nickname())                   // 닉네임(노출명)
                .name(dto.name())                           // 실명(내부용)
                .age(dto.age())                             // 나이
                .role(Role.USER)                            // 기본 권한
                .trustScore(DEFAULT_TRUST_SCORE)            // 기본 신뢰 점수
                .emailVerified(true)                        // 인증 완료 플래그
                .build();

        User saved = userRepository.save(user);             // 영속화

        // 4) 아바타(이니셜·색상·SVG Data URL) 확정 → 엔티티에 세팅
        avatarGenerator.applyOnSignup(saved);

        userRepository.save(saved);                         // 아바타 정보 반영 재저장

        // 5) 사용된 인증 상태/코드 제거(예: Redis 플래그)
        emailService.clearVerifiedFlag(dto.email());

        // 6) 결과 로그 및 ID 반환
        log.info("[UserService] 회원가입 완료. email={}", saved.getEmail());
        return saved.getId();
    }

    /**
     * 닉네임 변경(이 프로젝트는 email=아이디).
     * 1) 닉네임 형식 검증
     * 2) 사용자 조회
     * 3) 동일 닉네임이면 무변경 처리
     * 4) 본인 제외 중복 체크
     * 5) 닉네임 변경 + 아바타 재생성
     * 6) 저장
     */
    @Transactional
    public void updateNicknameByEmail(String email, String newNickname) {
        // 1) 닉네임 기초 검증
        if (newNickname == null || newNickname.isBlank())
            throw new BusinessException(ErrorCode.INVALID_NICKNAME);
        int len = newNickname.codePointCount(0, newNickname.length());
        if (len < 2 || len > 10)
            throw new BusinessException(ErrorCode.INVALID_NICKNAME);

        // 2) 사용자 조회
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 3) 동일 닉네임이면 조용히 통과
        if (newNickname.equals(user.getNickname())) {
            log.info("[UserService] 닉네임 동일, 변경 없음. email={}, nickname={}", email, newNickname);
            return;
        }

        // 4) 본인 제외 닉네임 중복 검사
        if (userRepository.existsByNicknameAndEmailNot(newNickname, email)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 5) 닉네임 변경 및 아바타 재생성(닉네임 기반 색/이니셜/이미지)
        user.changeNickname(newNickname);
        avatarGenerator.applyOnNicknameChange(user);

        // 6) 저장
        userRepository.save(user);
        log.info("[UserService] 닉네임 변경 및 아바타 재생성 완료. email={}, newNickname={}", email, newNickname);
    }

    /**
     * 비밀번호 재설정.
     * - 사용자 조회 → 비밀번호 해싱 → 저장
     */
    @Transactional
    public void resetPassword(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND)); // 사용자 검증
        String encoded = passwordEncoder.encode(rawPassword);                        // 해싱
        user.setPassword(encoded);                                                   // 세팅
        userRepository.save(user);                                                   // 저장
        log.info("[UserService] 비밀번호 재설정 완료. email={}", email);
    }

    /**
     * 이메일 중복 여부 조회(비즈니스 규칙상 외부 노출 가능).
     */
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 닉네임 중복 여부 조회(비즈니스 규칙상 외부 노출 가능).
     */
    @Transactional(readOnly = true)
    public boolean isNicknameExists(String nickname) {
        return userRepository.existsByNickname(nickname);
    }
}
