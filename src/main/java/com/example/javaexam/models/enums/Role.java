package com.example.javaexam.models.enums;

/**
 * Application roles. Stored in the database as a string (see {@code users.role}).
 * Spring Security authorities are derived as {@code "ROLE_" + name()}
 * (e.g. {@code ROLE_USER}, {@code ROLE_ADMIN}).
 */
public enum Role {
    USER,
    ADMIN
}
