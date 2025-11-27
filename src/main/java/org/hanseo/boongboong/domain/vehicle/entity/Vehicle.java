package org.hanseo.boongboong.domain.vehicle.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.global.entity.BaseEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "vehicles")
public class Vehicle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User owner;

    @Column(name = "vehicle_number", nullable = false, unique = true)
    private String number;

    @Column(name = "vehicle_image_url")
    private String imageUrl;

    @Column(name = "seats")
    private Integer seats;

    @Column(name = "color", length = 30)
    private String color;

    @Builder
    public Vehicle(User owner, String number, String imageUrl, Integer seats, String color) {
        this.owner = owner;
        this.number = number;
        this.imageUrl = imageUrl;
        this.seats = seats;
        this.color = color;
    }

    public void update(String number, String imageUrl, Integer seats, String color) {
        if (number != null) this.number = number;
        if (imageUrl != null) this.imageUrl = imageUrl;
        if (seats != null) this.seats = seats;
        if (color != null) this.color = color;
    }
}

