// src/main/java/.../notify/DevNotifyController.java
package org.hanseo.boongboong.notify;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dev")
public class DevNotifyController {
    private final InAppNotifyService notify;

    @PostMapping("/notify")
    public void notifyTest(@RequestParam String to, @RequestParam(defaultValue = "SYSTEM") NotificationType type) {
        notify.toUser(to, type, null, null, "개발용 테스트 알림: " + type);
    }
}
