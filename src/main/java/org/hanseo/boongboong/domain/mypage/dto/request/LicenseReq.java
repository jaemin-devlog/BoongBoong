package org.hanseo.boongboong.domain.mypage.dto.request;

import java.time.LocalDate;

public record LicenseReq(
        String licenseNumber,
        String licenseType,
        LocalDate issuedAt,
        LocalDate expiresAt,
        String name,
        LocalDate birthDate,
        String address
) {}

