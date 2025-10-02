// src/main/java/.../notify/InAppNotifyService.java
package org.hanseo.boongboong.notify;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InAppNotifyService {
    private final SimpMessagingTemplate messaging;

    public void toUser(String email, NotificationType type, Long matchId, Long requestId, String message) {
        NotificationPayload payload = NotificationPayload.builder()
                .type(type)
                .matchId(matchId)
                .requestId(requestId)
                .message(message)
                .createdAt(Instant.now())
                .build();
        // 개인 큐: /user/{email}/queue/notify
        messaging.convertAndSendToUser(email, "/queue/notify", payload);
    }

    public void toMatchTopic(Long matchId, NotificationType type, String message) {
        NotificationPayload payload = NotificationPayload.builder()
                .type(type)
                .matchId(matchId)
                .message(message)
                .createdAt(Instant.now())
                .build();
        // 방 전체 브로드캐스트(선택)
        messaging.convertAndSend("/topic/match/" + matchId, payload);
    }
}
