package org.hanseo.boongboong.domain.match.type;

public enum MatchStatus {
    OPEN,       // 좌석 여유, 매칭 진행 중
    LOCKED,     // 좌석 가득 참
    COMPLETED,  // 운행 완료
    CANCELLED   // 매칭 자체 취소
}
