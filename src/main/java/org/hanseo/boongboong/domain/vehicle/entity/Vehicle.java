package org.hanseo.boongboong.domain.vehicle.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.global.entity.BaseEntity;

/**
 * 차량 엔티티.
 * - 사용자 1:1 차량 등록(번호/이미지 URL) 정보 보관
 * - owner_id 유니크 보장으로 사용자당 1대만 매핑
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User owner; // 소유 사용자(1:1)

    @Column(name = "vehicle_number", nullable = false, unique = true)
    private String number; // 차량 번호(전국 유일 가정)

    @Column(name = "vehicle_image_url")
    private String imageUrl; // 차량 이미지 URL

    @Builder
    public Vehicle(User owner, String number, String imageUrl) {
        this.owner = owner;       // 소유자 설정
        this.number = number;     // 차량번호 설정
        this.imageUrl = imageUrl; // 이미지 URL 설정
    }
}
