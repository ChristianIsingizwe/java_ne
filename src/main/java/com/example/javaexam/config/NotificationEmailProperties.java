package com.example.javaexam.config;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "app.notifications.email")
public class NotificationEmailProperties {

    private boolean enabled;

    private String apiKey;

    @Email
    private String from;

    @Email
    private String replyTo;
}
