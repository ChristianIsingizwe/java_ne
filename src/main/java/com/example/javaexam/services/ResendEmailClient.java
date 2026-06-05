package com.example.javaexam.services;

import com.example.javaexam.config.NotificationEmailProperties;
import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ResendEmailClient {

    private final NotificationEmailProperties properties;

    public void send(String to, String subject, String html, String text) {
        if (isBlank(properties.getApiKey())) {
            throw new IllegalStateException("Notification email delivery is enabled but RESEND_API_KEY is not configured");
        }
        if (isBlank(properties.getFrom())) {
            throw new IllegalStateException("Notification email delivery is enabled but RESEND_FROM_EMAIL is not configured");
        }

        Resend resend = new Resend(properties.getApiKey());
        CreateEmailOptions.Builder options = CreateEmailOptions.builder()
                .from(properties.getFrom())
                .to(to)
                .subject(subject)
                .html(html)
                .text(text);
        if (!isBlank(properties.getReplyTo())) {
            options.replyTo(properties.getReplyTo());
        }

        try {
            CreateEmailResponse response = resend.emails().send(options.build());
            log.info("Sent notification email to {} via Resend with id {}", to, response.getId());
        } catch (ResendException ex) {
            throw new IllegalStateException("Failed to send notification email via Resend", ex);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
