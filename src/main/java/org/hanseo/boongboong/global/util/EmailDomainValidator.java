package org.hanseo.boongboong.global.util;

import lombok.RequiredArgsConstructor;
import org.hanseo.boongboong.global.config.AllowedDomainsProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailDomainValidator {
    private final AllowedDomainsProperties allowedDomainsProperties;

    public boolean isAllowed(String email) {
        int at = email.lastIndexOf('@');
        if (at <= 0) return false;
        String domain = email.substring(at + 1);
        return allowedDomainsProperties.getAllowed().contains(domain.toLowerCase());
    }
}
