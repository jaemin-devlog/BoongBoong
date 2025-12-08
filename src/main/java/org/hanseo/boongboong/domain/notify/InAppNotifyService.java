package org.hanseo.boongboong.domain.notify;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Lightweight in-app notification service backed by Server-Sent Events (SSE).
 * - Each authenticated user can open an SSE stream; we push events into it.
 * - Other services can call the same API surface to deliver events.
 */
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
        emittersByUser.computeIfAbsent(email, k -> new ArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(email, emitter));
        emitter.onTimeout(() -> removeEmitter(email, emitter));
        emitter.onError(e -> removeEmitter(email, emitter));

        try {
            SseEmitter.SseEventBuilder builder = SseEmitter.event()
                    .name("init")
                    .data("connected", MediaType.TEXT_PLAIN)
                    .reconnectTime(3000);
            emitter.send(builder);
        } catch (IOException ignored) {
            removeEmitter(email, emitter);
        }
        return emitter;
    }

    private void removeEmitter(String email, SseEmitter emitter) {
        List<SseEmitter> list = emittersByUser.get(email);
        if (list != null) list.remove(emitter);
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
        for (SseEmitter emitter : list) {
            try {
                SseEmitter.SseEventBuilder builder = SseEmitter.event()
                        .name("notify")
                        .data(event, MediaType.APPLICATION_JSON)
                        .reconnectTime(3000);
                emitter.send(builder);
            } catch (IOException e) {
                broken.add(emitter);
            }
        }
        if (!broken.isEmpty()) list.removeAll(broken);
    }
}

