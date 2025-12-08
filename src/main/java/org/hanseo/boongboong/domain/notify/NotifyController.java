package org.hanseo.boongboong.domain.notify;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notify")
@Slf4j
public class NotifyController {

    private final InAppNotifyService notifyService;

    /**
     * SSE stream for the authenticated user.
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(@AuthenticationPrincipal(expression = "username") String email,
                             HttpServletResponse response,
                             HttpServletRequest request) {
        // Best-effort headers to improve SSE compatibility behind proxies/LBs
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("X-Accel-Buffering", "no"); // nginx: disable buffering
        response.setCharacterEncoding("UTF-8");
        String origin = request.getHeader("Origin");
        String ua = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();
        log.debug("[SSE] stream request email={} ip={} origin={} ua={}", email, ip, origin, ua);
        return notifyService.subscribe(email);
    }
}
