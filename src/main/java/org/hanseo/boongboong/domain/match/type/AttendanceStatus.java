package org.hanseo.boongboong.domain.match.type;

public enum AttendanceStatus {
    UNKNOWN,    // 기본값(아직 결과 미확정)
    ATTENDED,   // 정상 탑승/완료
    NO_SHOW,    // 노쇼(벌점 -10)
    CANCELLED   // 취소(당일취소일 경우 벌점 -5)
}