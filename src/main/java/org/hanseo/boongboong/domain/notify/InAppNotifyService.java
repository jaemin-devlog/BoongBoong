package org.hanseo.boongboong.domain.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Lightweight in-app notification service backed by Server-Sent Events (SSE).
 * - Each authenticated user can open an SSE stream; we push events into it.
 * - Other services can call the same API surface to deliver events.
 */
@Slf4j
@Service
public class InAppNotifyService {

    public record Event(
            NotificationType type,
            Long matchId,
            Long requestId,
            String message,
            String openChatUrl,
            long ts
    ) {}

    private static final long DEFAULT_TIMEOUT_MS = 60L * 60 * 1000; // 1 hour

    private final Map<String, List<SseEmitter>> emittersByUser = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String email) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);
        // Use CopyOnWriteArrayList to avoid ConcurrentModificationException while iterating during sends
        emittersByUser.computeIfAbsent(email, k -> new CopyOnWriteArrayList<>()).add(emitter);

        log.debug("[SSE] subscribe start email={} total={}", email, emittersByUser.get(email).size());

        emitter.onCompletion(() -> {
            log.debug("[SSE] onCompletion email={}", email);
            removeEmitter(email, emitter);
        });
        emitter.onTimeout(() -> {
            log.debug("[SSE] onTimeout email={}", email);
            removeEmitter(email, emitter);
        });
        emitter.onError(e -> {
            log.debug("[SSE] onError email={} err={}", email, e.toString());
            removeEmitter(email, emitter);
        });

        try {
            SseEmitter.SseEventBuilder builder = SseEmitter.event()
                    .name("init")
                    .data("connected", MediaType.TEXT_PLAIN)
                    .reconnectTime(3000);
            emitter.send(builder);
        } catch (IOException ignored) {
            log.debug("[SSE] init send failed email={}", email);
            removeEmitter(email, emitter);
        }
        return emitter;
    }

    private void removeEmitter(String email, SseEmitter emitter) {
        List<SseEmitter> list = emittersByUser.get(email);
        if (list != null) {
            list.remove(emitter);
            if (list.isEmpty()) {
                // Best-effort cleanup to avoid empty lists lingering
                emittersByUser.remove(email, list);
            }
            log.debug("[SSE] emitter removed email={} remain={}", email, list.size());
        }
    }

    public void toUser(String email, NotificationType type, Long matchId, Long requestId, String message) {
        send(email, new Event(type, matchId, requestId, message, null, Instant.now().toEpochMilli()));
    }

    public void toUser(String email, NotificationType type, Long matchId, Long requestId, String message, String openChatUrl) {
        send(email, new Event(type, matchId, requestId, message, openChatUrl, Instant.now().toEpochMilli()));
    }

    public void toMatchTopic(Long matchId, NotificationType type, String message) {
        // Optional: broadcast to all users in the match if needed in future
    }

    private void send(String email, Event event) {
        List<SseEmitter> list = emittersByUser.get(email);
        if (list == null || list.isEmpty()) return;

        List<SseEmitter> broken = new ArrayList<>();
        log.debug("[SSE] send email={} type={} targets={}", email, event.type(), list.size());
        for (SseEmitter emitter : list) {
            try {
                SseEmitter.SseEventBuilder builder = SseEmitter.event()
                        .name("notify")
                        .data(event, MediaType.APPLICATION_JSON)
                        .reconnectTime(3000);
                emitter.send(builder);
            } catch (IOException e) {
                log.debug("[SSE] send error email={} err={}", email, e.toString());
                broken.add(emitter);
            }
        }
        if (!broken.isEmpty()) {
            list.removeAll(broken);
            log.debug("[SSE] removed broken emitters email={} count={}", email, broken.size());
        }
    }
}
