package org.hanseo.boongboong.domain.user.service;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailVerificationStatus {
    private final Map<String, Boolean> verifiedEmails = new ConcurrentHashMap<>();

    public void setVerified(String email) {
        verifiedEmails.put(email, true);
    }

    public boolean isVerified(String email) {
        return verifiedEmails.getOrDefault(email, false);
    }

    public void removeVerified(String email) {
        verifiedEmails.remove(email);
    }
}
