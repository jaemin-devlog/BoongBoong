package org.hanseo.boongboong.domain.notify;

import org.springframework.stereotype.Service;

/**
 * No-op notification service for demo without WebSocket/broker.
 * Keeps the API surface so other services can call it safely.
 */
@Service
public class InAppNotifyService {
    public void toUser(String email, NotificationType type, Long matchId, Long requestId, String message) {
        // no-op
    }

    public void toMatchTopic(Long matchId, NotificationType type, String message) {
        // no-op
    }
}

