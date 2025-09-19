package org.hanseo.boongboong.global.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

/**
 * 이메일 HTML 템플릿을 Thymeleaf로 렌더링하는 유틸.
 */
@Component
@RequiredArgsConstructor
public class MailTemplateRenderer {

    private final TemplateEngine templateEngine;

    public String renderVerificationHtml(Map<String, String> tokens) {
        Context context = new Context();
        tokens.forEach(context::setVariable);
        return templateEngine.process("verification", context);
    }
}
