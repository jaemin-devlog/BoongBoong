package org.hanseo.boongboong.global.mail;

public interface MailClient {
    void sendHtml(String to, String subject, String html);
}

