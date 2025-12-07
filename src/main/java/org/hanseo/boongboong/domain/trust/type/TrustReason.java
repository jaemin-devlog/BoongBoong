// src/main/java/org/hanseo/boongboong/domain/trust/type/TrustReason.java
package org.hanseo.boongboong.domain.trust.type;

public enum TrustReason {
    SAME_DAY_CANCEL, // 당일취소 -5
    NO_SHOW,         // 노쇼 -10
    REVIEW           // 리뷰에 의한 가점 (+)
}
