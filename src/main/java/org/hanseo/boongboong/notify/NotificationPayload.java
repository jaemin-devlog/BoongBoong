// src/main/java/.../notify/NotificationPayload.java
package org.hanseo.boongboong.notify;

import lombok.Builder;
import lombok.Value;
import java.time.Instant;

@Value
@Builder
public class NotificationPayload {
    NotificationType type;
    Long matchId;
    Long requestId;
    String message;
    Instant createdAt;
}
