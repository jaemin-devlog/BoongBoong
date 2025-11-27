package org.hanseo.boongboong.domain.license.repository;

import org.hanseo.boongboong.domain.license.entity.DriverLicense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverLicenseRepository extends JpaRepository<DriverLicense, Long> {
    Optional<DriverLicense> findByOwnerId(Long ownerId);
}

