package org.hanseo.boongboong.global.config;

import org.hanseo.boongboong.global.mail.MailClient;
import org.hanseo.boongboong.global.mail.SmtpMailClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
public class MailClientConfig {

    @Bean
    public MailClient mailClient(
            @Value("${app.mail.from:}") String from,
            JavaMailSender javaMailSender
    ) {
        return new SmtpMailClient(javaMailSender, from);
    }
}
