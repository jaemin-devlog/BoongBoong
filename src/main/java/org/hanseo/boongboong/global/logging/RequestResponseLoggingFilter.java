package org.hanseo.boongboong.global.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@Profile("dev")
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final String HDR_REQUEST_ID = "X-Request-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        long startNs = System.nanoTime();

        // Only trace SSE endpoint traffic
        String reqUri = request.getRequestURI();
        if (reqUri == null || !reqUri.startsWith("/api/notify/stream")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestId = Optional.ofNullable(request.getHeader(HDR_REQUEST_ID))
                .filter(s -> !s.isBlank())
                .orElse(UUID.randomUUID().toString());

        response.setHeader(HDR_REQUEST_ID, requestId);

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String qs = Optional.ofNullable(request.getQueryString()).map(q -> "?" + q).orElse("");
        String origin = Optional.ofNullable(request.getHeader("Origin")).orElse("");
        String ua = Optional.ofNullable(request.getHeader("User-Agent")).orElse("");

        log.info("REQ id={} {} {}{} origin={} ua={}", requestId, method, uri, qs, origin, ua);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long tookMs = (System.nanoTime() - startNs) / 1_000_000;
            String pattern = Optional.ofNullable((String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE)).orElse("");
            int status = response.getStatus();

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String user = (auth != null && auth.isAuthenticated()) ? String.valueOf(auth.getName()) : "-";

            log.info("RES id={} {} {} -> {} {}ms user={} pattern={} ", requestId, method, uri, status, tookMs, user, pattern);
        }
    }
}
