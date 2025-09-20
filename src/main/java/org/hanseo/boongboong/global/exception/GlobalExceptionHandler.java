package org.hanseo.boongboong.global.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    /** @RequestBody 검증 실패 (Bean Validation) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        var ec = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(ec.getStatus())
                .body(ErrorResponse.of(ec, req.getRequestURI(), fieldErrors));
    }

    /** @ModelAttribute / 쿼리 파라미터 바인딩 실패 */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBind(BindException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
        var ec = ErrorCode.INVALID_INPUT;
        return ResponseEntity.status(ec.getStatus())
                .body(ErrorResponse.of(ec, req.getRequestURI(), fieldErrors));
    }

    /** 405 Method Not Allowed */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethod(HttpRequestMethodNotSupportedException ex,
                                                      HttpServletRequest req) {
        var ec = ErrorCode.METHOD_NOT_ALLOWED;
        return ResponseEntity.status(ec.getStatus())
                .body(ErrorResponse.of(ec, req.getRequestURI()));
    }

    /** 403 Access Denied (인가 실패) */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleDenied(AccessDeniedException ex, HttpServletRequest req) {
        var ec = ErrorCode.ACCESS_DENIED;
        return ResponseEntity.status(ec.getStatus())
                .body(ErrorResponse.of(ec, req.getRequestURI()));
    }

    /**
     * 401 Authentication 실패
     * - 스프링 시큐리티 필터 레벨에서 터진 인증 예외
     * - 컨트롤러/서비스에서 던진 BusinessException(AUTHENTICATION_FAILED)은 아래 BusinessException 핸들러가 처리
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        var ec = ErrorCode.AUTHENTICATION_FAILED; // 401
        return ResponseEntity.status(ec.getStatus())
                .body(ErrorResponse.of(ec, req.getRequestURI()));
    }

    /**
     * 도메인 표준 비즈니스 예외
     * - AuthService: 인증 실패 → new BusinessException(AUTHENTICATION_FAILED)
     * - UserService: 중복/검증/도메인 위반 등
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBiz(BusinessException ex, HttpServletRequest req) {
        var ec = ex.getErrorCode();
        return ResponseEntity.status(ec.getStatus())
                .body(ErrorResponse.of(ec, req.getRequestURI()));
    }

    /** 404: 조회 실패 */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> handleNoSuch(NoSuchElementException ex, HttpServletRequest req) {
        var ec = ErrorCode.NOT_FOUND;
        return ResponseEntity.status(ec.getStatus())
                .body(ErrorResponse.of(ec, req.getRequestURI()));
    }

    /** 나머지 모든 예외 → 500 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest req) {
        var ec = ErrorCode.INTERNAL_ERROR;
        log.error("[500] uri={} msg={}", req.getRequestURI(), ex.getMessage(), ex);
        return ResponseEntity.status(ec.getStatus())
                .body(ErrorResponse.of(ec, req.getRequestURI()));
    }
}
