package org.hanseo.boongboong.domain.carpool.type;

/**
 * 카풀 게시글 상태 열거형.
 * - 모집/진행/완료/취소 상태를 표현
 */
public enum PostStatus {
    RECRUITING, // 모집중
    ONGOING,    // 진행중
    COMPLETED,  // 완료
    CANCELLED   // 취소
}
