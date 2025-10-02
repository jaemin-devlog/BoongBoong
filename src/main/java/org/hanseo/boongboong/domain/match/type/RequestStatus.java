package org.hanseo.boongboong.domain.match.type;

public enum RequestStatus {
    PENDING,    // 대기
    APPROVED,   // 수락됨 → 좌석 반영
    REJECTED,   // 거절됨
    CANCELLED   // 발신자 취소
}
