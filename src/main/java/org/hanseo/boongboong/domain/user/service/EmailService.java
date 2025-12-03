package org.hanseo.boongboong.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.hanseo.boongboong.global.mail.MailClient;
import org.hanseo.boongboong.global.mail.MailTemplateRenderer;
import org.hanseo.boongboong.global.util.EmailDomainValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Service
@RequiredArgsConstructor // DI 주입
public class EmailService {

    @Value("${app.mail.from:}")
    private String from;

    private final MailClient mailClient;                 // 메일 발송 객체
    private final MailTemplateRenderer templateRenderer;     // HTML 템플릿 렌더러
    private final EmailDomainValidator emailDomainValidator; // 이메일 도메인 허용 여부 검사

    // TODO: 다중 인스턴스 환경에서는 Redis로 외부화 고려
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();        // email -> 인증코드
    private final Map<String, LocalDateTime> codeCreationTimes = new ConcurrentHashMap<>(); // email -> 코드 생성시간
    private final Map<String, Integer> verificationAttempts = new ConcurrentHashMap<>();    // email -> 실패 횟수
    private final Map<String, Boolean> verifiedEmails = new ConcurrentHashMap<>();          // email -> 인증 완료 여부

    private static final long VERIFICATION_CODE_EXPIRATION_MINUTES = 5; // 인증코드 유효 시간 (5분)
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;             // 인증 실패 최대 횟수

    /** ===================== 회원가입용 이메일 인증 ===================== */

    /**
     * 인증 메일 발송
     */
    @org.springframework.scheduling.annotation.Async("mailTaskExecutor")
    @Transactional
    public void sendVerificationEmail(String email) {
        // 허용된 이메일 도메인인지 확인
        if (!emailDomainValidator.isAllowed(email)) {
            throw new BusinessException(ErrorCode.INVALID_EMAIL_DOMAIN);
        }

        // 기존 인증 성공 상태 제거
        verifiedEmails.remove(email);

        // 최근 1분 내 재발송 방지
        if (codeCreationTimes.containsKey(email) &&
                codeCreationTimes.get(email).plusMinutes(1).isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TOO_FREQUENT_EMAIL_REQUEST);
        }

        // 인증코드 생성
        String code = generateVerificationCode();
        verificationCodes.put(email, code);
        codeCreationTimes.put(email, LocalDateTime.now());
        verificationAttempts.put(email, 0); // 실패횟수 초기화

        try {
            // HTML 템플릿에 코드 삽입
            Map<String, String> tokens = new HashMap<>();
            tokens.put("code", code);
            String htmlContent = templateRenderer.renderVerificationHtml(tokens);

            // 메일 발송
            mailClient.sendHtml(email, "[붕붕] 이메일 인증코드", htmlContent);
            log.info("[EmailService] 인증 코드를 발송했습니다: {}", email);
        } catch (Exception e) {
            log.error("[EmailService] 인증 코드 발송 실패: {}: {}", email, e.getMessage());
            return;
        }
    }

    /**
     * 인증코드 검증
     */
    @Transactional
    public boolean verifyEmail(String email, String code) {
        // 저장된 값 없는 경우 → 만료
        if (!verificationCodes.containsKey(email) || !codeCreationTimes.containsKey(email)) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
        }

        // 5분 경과 → 만료
        if (codeCreationTimes.get(email)
                .plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES)
                .isBefore(LocalDateTime.now())) {
            verificationCodes.remove(email);
            codeCreationTimes.remove(email);
            verificationAttempts.remove(email);
            throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
        }

        // 코드 불일치 → 실패 증가
        if (!verificationCodes.get(email).equals(code)) {
            verificationAttempts.compute(email, (k, v) -> v == null ? 1 : v + 1);

            // 실패 최대 횟수 초과 시 코드 제거
            if (verificationAttempts.get(email) >= MAX_VERIFICATION_ATTEMPTS) {
                verificationCodes.remove(email);
                codeCreationTimes.remove(email);
                verificationAttempts.remove(email);
                log.warn("[EmailService] 인증 실패 횟수 초과: {}", email);
            }
            throw new BusinessException(ErrorCode.EMAIL_CODE_MISMATCH);
        }

        // 성공
        verificationCodes.remove(email);
        codeCreationTimes.remove(email);
        verificationAttempts.remove(email);
        verifiedEmails.put(email, true);
        log.info("[EmailService] Email verified: {}", email);
        return true;
    }

    /**
     * 이메일 인증 여부
     */
    public boolean isVerified(String email) {
        return verifiedEmails.containsKey(email);
    }

    /**
     * 인증 상태 초기화
     */
    public void clearVerifiedFlag(String email) {
        verificationCodes.remove(email);
        codeCreationTimes.remove(email);
        verificationAttempts.remove(email);
        verifiedEmails.remove(email);
    }

    /** ===================== 비밀번호 재설정 인증코드 ===================== */

    private final Map<String, String> resetCodes = new ConcurrentHashMap<>();               // email -> 재설정 코드
    private final Map<String, LocalDateTime> resetCreationTimes = new ConcurrentHashMap<>();// email -> 생성 시간
    private final Map<String, Integer> resetAttempts = new ConcurrentHashMap<>();           // email -> 실패 횟수
    private final Map<String, Boolean> resetVerifiedEmails = new ConcurrentHashMap<>();     // email -> 성공 여부

    private static final long RESET_CODE_EXPIRATION_MINUTES = 5; // 재설정코드 유효기간
    private static final int RESET_MAX_ATTEMPTS = 5;

    /**
     * 비밀번호 재설정 코드 발송
     */
    @org.springframework.scheduling.annotation.Async("mailTaskExecutor")
    @Transactional
    public void sendPasswordResetEmail(String email) {
        // 1분 내 재발송 제한
        if (resetCreationTimes.containsKey(email) &&
                resetCreationTimes.get(email).plusMinutes(1).isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TOO_FREQUENT_EMAIL_REQUEST);
        }

        String code = generateVerificationCode();
        resetCodes.put(email, code);
        resetCreationTimes.put(email, LocalDateTime.now());
        resetAttempts.put(email, 0);
        resetVerifiedEmails.remove(email);

        try {
            Map<String, String> tokens = new HashMap<>();
            tokens.put("code", code);
            String htmlContent = templateRenderer.renderVerificationHtml(tokens);

            mailClient.sendHtml(email, "[붕붕] 비밀번호 재설정 코드", htmlContent);
            log.info("[EmailService] 재설정 코드를 발송했습니다: {}", email);
        } catch (Exception e) {
            log.error("[EmailService] 재설정 코드 발송 실패 {}: {}", email, e.getMessage());
            return;
        }
    }

    /**
     * 비밀번호 재설정 코드 검증
     */
    @Transactional
    public boolean verifyPasswordResetCode(String email, String code) {
        if (!resetCodes.containsKey(email) || !resetCreationTimes.containsKey(email)) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
        }
        if (resetCreationTimes.get(email).plusMinutes(RESET_CODE_EXPIRATION_MINUTES)
                .isBefore(LocalDateTime.now())) {
            resetCodes.remove(email);
            resetCreationTimes.remove(email);
            resetAttempts.remove(email);
            throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
        }
        if (!resetCodes.get(email).equals(code)) {
            resetAttempts.compute(email, (k, v) -> v == null ? 1 : v + 1);

            if (resetAttempts.get(email) >= RESET_MAX_ATTEMPTS) {
                resetCodes.remove(email);
                resetCreationTimes.remove(email);
                resetAttempts.remove(email);
                log.warn("[EmailService] 재설정 코드 실패 횟수 초과: {}", email);
            }
            throw new BusinessException(ErrorCode.EMAIL_CODE_MISMATCH);
        }

        resetCodes.remove(email);
        resetCreationTimes.remove(email);
        resetAttempts.remove(email);
        resetVerifiedEmails.put(email, true);
        log.info("[EmailService] Reset code verified: {}", email);
        return true;
    }

    public boolean isPasswordResetVerified(String email) {
        return resetVerifiedEmails.containsKey(email);
    }

    public void clearPasswordResetFlag(String email) {
        resetCodes.remove(email);
        resetCreationTimes.remove(email);
        resetAttempts.remove(email);
        resetVerifiedEmails.remove(email);
    }

    /** ===================== 공통 유틸 & 정리 스케줄러 ===================== */

    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();

        // 회원가입용 코드 삭제
        codeCreationTimes.forEach((email, creationTime) -> {
            if (creationTime.plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES).isBefore(now)) {
                log.info("[EmailService] Cleaning expired signup code: {}", email);
                verificationCodes.remove(email);
                codeCreationTimes.remove(email);
                verificationAttempts.remove(email);
            }
        });

        // 재설정 코드 삭제
        resetCreationTimes.forEach((email, creationTime) -> {
            if (creationTime.plusMinutes(RESET_CODE_EXPIRATION_MINUTES).isBefore(now)) {
                log.info("[EmailService] Cleaning expired reset code: {}", email);
                resetCodes.remove(email);
                resetCreationTimes.remove(email);
                resetAttempts.remove(email);
            }
        });
    }
}
