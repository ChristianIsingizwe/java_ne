package com.example.javaexam.utils;

import com.example.javaexam.exceptions.ApiException;

public final class InputSanitizer {

    private InputSanitizer() {
    }

    public static String normalizeRequired(String value, String fieldName) {
        if (value == null) {
            throw ApiException.badRequest(fieldName + " is required");
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        if (normalized.isBlank()) {
            throw ApiException.badRequest(fieldName + " is required");
        }
        return normalized;
    }

    public static String normalizeEmail(String email) {
        return normalizeRequired(email, "Email").toLowerCase();
    }

    public static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }
}
