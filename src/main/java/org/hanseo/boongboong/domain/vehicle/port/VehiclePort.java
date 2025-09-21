package org.hanseo.boongboong.domain.vehicle.port;

import java.util.Optional;

/**
 * 차량 조회 포트(헥사고날 아키텍처 포트 인터페이스 예시).
 * - 애플리케이션 서비스가 의존하는 외부 조회 계약
 */
public interface VehiclePort { // 차량 읽기 포트
    Optional<VehicleInfo> primaryOf(Long userId); // 기본 차량 조회(없을 수 있으므로 Optional)

    record VehicleInfo(String no, String img) {}  // 차량 번호, 이미지 URL
}
