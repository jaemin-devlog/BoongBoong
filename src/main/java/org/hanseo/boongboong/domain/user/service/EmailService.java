package org.hanseo.boongboong.domain.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hanseo.boongboong.global.exception.BusinessException;
import org.hanseo.boongboong.global.exception.ErrorCode;
import org.hanseo.boongboong.global.mail.MailTemplateRenderer;
import org.hanseo.boongboong.global.util.EmailDomainValidator;
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

    private final JavaMailSender mailSender; // 메일 전송 객체
    private final MailTemplateRenderer templateRenderer; //HTML 템플릿 렌더링
    private final EmailDomainValidator emailDomainValidator; // 도메인 허용 검증

    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();        //email -> 인증코드
    private final Map<String, LocalDateTime> codeCreationTimes = new ConcurrentHashMap<>(); //email -> 코드 생성 시각
    private final Map<String, Integer> verificationAttempts = new ConcurrentHashMap<>();    // email → 실패 시도 횟수
    private final Map<String, Boolean> verifiedEmails = new ConcurrentHashMap<>();          // email → 인증 성공 여부

    private static final long VERIFICATION_CODE_EXPIRATION_MINUTES = 5; // 인증코드 유효 시간 (5분)
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;             // 최대 허용 실패 횟수

    /**
     * 인증 메일 발송
     */
    @Transactional
    public void sendVerificationEmail(String email) {
        if (!emailDomainValidator.isAllowed(email)) { // 학교 이메일인지 확인
            throw new BusinessException(ErrorCode.INVALID_EMAIL_DOMAIN);
        }

        // 이메일 재 요청 시 인증 상태 제거
        verifiedEmails.remove(email);

        // 최근 1분 내 발송 이력이 있으면 재요청 차단
        if (codeCreationTimes.containsKey(email) &&
                codeCreationTimes.get(email).plusMinutes(1).isAfter(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.TOO_FREQUENT_EMAIL_REQUEST);
        }

        // 6자리 난수 코드 생성 및 저장
        String code = generateVerificationCode();
        verificationCodes.put(email, code);
        codeCreationTimes.put(email, LocalDateTime.now());
        verificationAttempts.put(email, 0); // 실패횟수 초기화

        try {
            //메일 메시지 생성
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email); // 수신자
            helper.setSubject("[붕붕] 이메일 인증 코드");

            // HTML 템플릿에 코드 삽입
            Map<String, String> tokens = new HashMap<>();
            tokens.put("code", code);
            String htmlContent = templateRenderer.renderVerificationHtml(tokens);
            helper.setText(htmlContent, true);

            //메일 발송
            mailSender.send(message);
            log.info("[EmailService] 인증 코드를 보냈습니다. {}", email);
        } catch (MessagingException e) {
            log.error("[EmailService] 인증 코드를 보내는 데 실패했습니다. {}: {}", email, e.getMessage());
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * 인증 코드 검증
     */
    @Transactional
    public boolean verifyEmail(String email, String code) {
        // 코드/생성 시간 미존재 → 만료 처리
        if (!verificationCodes.containsKey(email) || !codeCreationTimes.containsKey(email)) {
            throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
        }

        // 코드가 유효시간(5분) 초과 시 만료 처리
        if (codeCreationTimes.get(email).plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES).isBefore(LocalDateTime.now())) {
            verificationCodes.remove(email);
            codeCreationTimes.remove(email);
            verificationAttempts.remove(email);
            throw new BusinessException(ErrorCode.EMAIL_CODE_EXPIRED);
        }

        // 코드 불일치 시 실패 횟수 증가
        if (!verificationCodes.get(email).equals(code)) {
            verificationAttempts.compute(email, (k, v) -> v == null ? 1 : v + 1);

            // 실패 횟수 초과 시 해당 이메일 코드 정보 삭제
            if (verificationAttempts.get(email) >= MAX_VERIFICATION_ATTEMPTS) {
                verificationCodes.remove(email);
                codeCreationTimes.remove(email);
                verificationAttempts.remove(email);
                log.warn("[EmailService] 실패 횟수가 초과되었습니다. {}", email);
            }
            throw new BusinessException(ErrorCode.EMAIL_CODE_MISMATCH);
        }

        // 성공: 관련 데이터 초기화 + 인증 성공 플래그 저장
        verificationCodes.remove(email);
        codeCreationTimes.remove(email);
        verificationAttempts.remove(email);
        verifiedEmails.put(email, true); // Mark as verified
        log.info("[EmailService] Email verified: {}", email);
        return true;
    }

    /**
     * 해당 이메일 인증 여부 확인
     */
    public boolean isVerified(String email) {
        return verifiedEmails.containsKey(email);
    }

    /**
     * 가입 완료 후 인증 데이터 삭제
     */
    public void clearVerifiedFlag(String email) {
        verificationCodes.remove(email);
        codeCreationTimes.remove(email);
        verificationAttempts.remove(email);
        verifiedEmails.remove(email); // Also clear from verifiedEmails
    }

    /**
     * 6자리 랜덤 인증 코드 생성
     */
    private String generateVerificationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    /**
     *  스케줄러 : 만료된 코드 정리 (1분마다 실행)
     */
    @Scheduled(fixedRate = 60000) // Run every 1 minute
    public void cleanupExpiredCodes() {
        LocalDateTime now = LocalDateTime.now();
        codeCreationTimes.forEach((email, creationTime) -> {
            if (creationTime.plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES).isBefore(now)) {
                log.info("[EmailService] Cleaning up expired code for {}", email);
                verificationCodes.remove(email);
                codeCreationTimes.remove(email);
                verificationAttempts.remove(email);
                // verifiedEmails는 성공한 경우만 담기므로 여기서 제거하지 않음
            }
        });
    }
}
