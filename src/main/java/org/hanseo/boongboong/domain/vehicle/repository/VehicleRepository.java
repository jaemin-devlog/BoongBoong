package org.hanseo.boongboong.domain.vehicle.repository;

import org.hanseo.boongboong.domain.vehicle.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Vehicle 엔티티 리포지토리.
 * - 사용자 기준 차량 단건 조회 제공
 */
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByOwnerId(Long ownerId); // 사용자 ID로 1:1 차량 조회
}
