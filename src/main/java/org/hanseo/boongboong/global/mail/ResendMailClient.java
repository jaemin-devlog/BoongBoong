package org.hanseo.boongboong.global.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ResendMailClient implements MailClient {
    private final WebClient webClient;
    private final String from;

    @Override
    public void sendHtml(String to, String subject, String html) {
        // Resend API: POST https://api.resend.com/emails
        // { from, to, subject, html }
        webClient.post()


                .uri("/emails")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new Payload(from, to, subject, html))
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(ex -> Mono.error(new RuntimeException("Resend send failed: " + ex.getMessage(), ex)))
                .block();
    }

    private record Payload(String from, String to, String subject, String html){}
}

