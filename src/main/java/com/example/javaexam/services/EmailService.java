package com.example.javaexam.services;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import com.example.javaexam.exceptions.ApiException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Sends transactional emails through the Resend API.
 */
@Service
@Slf4j
public class EmailService {

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${app.mail.resend.api-key:}")
    private String resendApiKey;

    private Resend resend;

    @PostConstruct
    void initialize() {
        if (!mailEnabled) {
            return;
        }
        if (!StringUtils.hasText(resendApiKey)) {
            throw new IllegalStateException(
                    "app.mail.resend.api-key must be set when app.mail.enabled=true");
        }
        resend = new Resend(resendApiKey);
    }

    public void sendVerificationEmail(String to, String firstName, String verificationUrl) {
        if (!mailEnabled) {
            log.info("Mail disabled. Verification link for {}: {}", to, verificationUrl);
            return;
        }

        send(to, "Verify your email address", """
                Hi %s,

                Thanks for registering. Please confirm your email address by opening the link below:

                %s

                If you did not create this account, you can safely ignore this email.
                """.formatted(firstName, verificationUrl), "Verification email sent to {}", to);
    }

    public void sendPasswordResetEmail(String to, String firstName, String resetUrl) {
        if (!mailEnabled) {
            log.info("Mail disabled. Password reset link for {}: {}", to, resetUrl);
            return;
        }

        send(to, "Reset your password", """
                Hi %s,

                We received a request to reset your password. Use the link below to choose a new one:

                %s

                This link expires shortly. If you did not request a password reset, you can safely
                ignore this email and your password will remain unchanged.
                """.formatted(firstName, resetUrl), "Password-reset email sent to {}", to);
    }

    private void send(String to, String subject, String text, String logMessage, String logTarget) {
        try {
            var builder = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .text(text);
            CreateEmailResponse response = resend.emails().send(builder.build());
            log.info(logMessage + " (id={})", logTarget, response.getId());
        } catch (ResendException ex) {
            log.warn("Failed to send email to {}: {}", to, ex.getMessage());
            throw ApiException.serviceUnavailable("Email service is unavailable. Please try again later.");
        }
    }
}
