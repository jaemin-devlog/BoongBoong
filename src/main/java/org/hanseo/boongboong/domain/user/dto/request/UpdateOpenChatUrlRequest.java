package org.hanseo.boongboong.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateOpenChatUrlRequest(
        @NotBlank String openChatUrl
) {}

