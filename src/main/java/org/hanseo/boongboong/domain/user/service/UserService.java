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

@Slf4j // Slf4j 로거를 자동으로 생성 (log 변수 사용 가능)
@Service
@RequiredArgsConstructor // final로 선언된 모든 필드를 인자로 받는 생성자를 자동 생성 (의존성 주입)
@Transactional(readOnly = true) // 클래스 내 모든 public 메서드에 읽기 전용 트랜잭션을 기본으로 적용
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailDomainValidator emailDomainValidator; // 허용된 이메일을 검증
    private final PasswordEncoder passwordEncoder;

    private static final int DEFAULT_TRUST_SCORE = 50; // 회원가입 시 기본으로 부여되는 신뢰 점수
    private static final int DEFAULT_POINTS = 50; // 회원가입 시 기본으로 부여되는 포인트

    @Transactional
    public Long signup(SignUpRequestDto dto) { // 회원가입
        // 회원가입 검증
        if (!emailDomainValidator.isAllowed(dto.email())) { // 학교 이메일이 아닌 경우
            throw new BusinessException(ErrorCode.INVALID_EMAIL_DOMAIN);
        }
        if (!emailService.isVerified(dto.email())) { // 이메일 인증이 완료되지 않은 경우
            throw new BusinessException(ErrorCode.EMAIL_NOT_VERIFIED);
        }
        if (userRepository.existsByEmail(dto.email())) { // 이미 존재하는 이메일인 경우
            // DUPLICATE_EMAIL → EMAIL_ALREADY_EXISTS 로 변경
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(dto.nickname())) { // 이미 존재하는 닉네임인 경우
            // DUPLICATE_NICKNAME → NICKNAME_ALREADY_EXISTS 로 변경
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        String encodedPw = passwordEncoder.encode(dto.password()); // 사용자의 비밀번호를 암호화

        // DTO로부터 받은 정보와 기본값을 사용하여 User 엔티티 생성
        User user = User.builder()
                .email(dto.email())         //이메일
                .password(encodedPw)        //비밀번호
                .nickname(dto.nickname())   //닉네임
                .name(dto.name())           //실명
                .age(dto.age())             //나이
                .role(Role.USER)            //기본 권한 - USER
                .trustScore(DEFAULT_TRUST_SCORE) // 기본 신뢰 점수 설정
                .points(DEFAULT_POINTS)     // 기본 포인트 설정
                .emailVerified(true)        // 이메일 인증 완료 상태로 설정
                .build();

        User saved = userRepository.save(user); // user 엔티티 db에 저장
        emailService.clearVerifiedFlag(dto.email()); // 회원가입이 완료 -> 사용된 이메일 인증 정보(Redis) 제거

        log.info("[UserService] 회원가입 완료. userId={}, email={}", saved.getId(), saved.getEmail()); // 회원가입 완료 로그 기록
        return saved.getId(); // 생성된 ID 반환
    }
    @Transactional
    public void resetPassword(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        String encoded = passwordEncoder.encode(rawPassword);
        user.setPassword(encoded);
        userRepository.save(user);
        log.info("[UserService] 비밀번호 재설정 완료. email={}", email);
    }


    @Transactional(readOnly = true) // 데이터 조회 읽기 전용 트랜잭션 적용
    public boolean isEmailExists(String email) {    // 이메일 중복 여부
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true) // 데이터 조회 읽기 전용 트랜잭션 적용
    public boolean isNicknameExists(String nickname) { // 닉네임 중복 여부
        return userRepository.existsByNickname(nickname);
    }
}
