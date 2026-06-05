package com.example.javaexam.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Enables {@code @Scheduled} tasks (used by the token-cleanup job). */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
