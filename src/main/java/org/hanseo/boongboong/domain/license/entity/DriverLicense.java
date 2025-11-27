package org.hanseo.boongboong.domain.license.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hanseo.boongboong.domain.user.entity.User;
import org.hanseo.boongboong.global.entity.BaseEntity;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
@Table(name = "driver_license", uniqueConstraints = @UniqueConstraint(name = "uk_license_owner", columnNames = {"owner_id"}))
public class DriverLicense extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false, unique = true)
    private User owner;

    @Column(name = "license_number", nullable = false, length = 50)
    private String licenseNumber;

    @Column(name = "license_type", length = 30)
    private String licenseType;

    private LocalDate issuedAt;
    private LocalDate expiresAt;

    @Column(length = 50)
    private String name;

    private LocalDate birthDate;

    @Column(length = 200)
    private String address;

    public void update(String licenseNumber, String licenseType, LocalDate issuedAt, LocalDate expiresAt,
                       String name, LocalDate birthDate, String address) {
        if (licenseNumber != null) this.licenseNumber = licenseNumber;
        if (licenseType != null) this.licenseType = licenseType;
        if (issuedAt != null) this.issuedAt = issuedAt;
        if (expiresAt != null) this.expiresAt = expiresAt;
        if (name != null) this.name = name;
        if (birthDate != null) this.birthDate = birthDate;
        if (address != null) this.address = address;
    }
}

