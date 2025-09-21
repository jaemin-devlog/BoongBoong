package org.hanseo.boongboong.domain.carpool.port;

import java.util.Optional;

public interface VehiclePort { // 차량 읽기 포트
    Optional<VehicleInfo> primaryOf(Long userId); // 기본 차량 조회
    record VehicleInfo(String no, String img) {}  // 번호, 이미지
}
