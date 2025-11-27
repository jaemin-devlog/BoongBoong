package org.hanseo.boongboong.domain.mypage.dto.response;

import java.time.LocalDate;

public record LicenseRes(
        String licenseNumber,
        String licenseType,
        LocalDate issuedAt,
        LocalDate expiresAt,
        String name,
        LocalDate birthDate,
        String address
) {}

