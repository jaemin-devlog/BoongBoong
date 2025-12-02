package org.hanseo.boongboong.global.config;

import org.hanseo.boongboong.global.mail.MailClient;
import org.hanseo.boongboong.global.mail.ResendMailClient;
import org.hanseo.boongboong.global.mail.SmtpMailClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class MailClientConfig {

    @Bean
    public MailClient mailClient(
            @Value("${app.mail.provider:smtp}") String provider,
            @Value("${app.mail.from:}") String from,
            @Value("${RESEND_API_KEY:}") String resendApiKey,
            JavaMailSender javaMailSender
    ) {
        if ("resend".equalsIgnoreCase(provider)) {
            WebClient wc = WebClient.builder()
                    .baseUrl("https://api.resend.com")
                    .defaultHeader("Authorization", "Bearer " + resendApiKey)
                    .build();
            return new ResendMailClient(wc, from);
        }
        return new SmtpMailClient(javaMailSender, from);
    }
}

