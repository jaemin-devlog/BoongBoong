package org.hanseo.boongboong.domain.auth.dto.request;

public record ResetPasswordRequest(String email, String newPassword) {}
