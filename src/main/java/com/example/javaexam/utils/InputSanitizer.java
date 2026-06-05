package com.example.javaexam.utils;

import com.example.javaexam.exceptions.ApiException;
import java.util.LinkedHashSet;
import java.util.Set;

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

    public static String normalizeRwandanPhoneNumber(String phoneNumber) {
        String normalized = normalizeRequired(phoneNumber, "Phone number").replaceAll("[\\s-]", "");
        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        }
        if (normalized.startsWith("250")) {
            normalized = normalized.substring(3);
        } else if (normalized.startsWith("0")) {
            normalized = normalized.substring(1);
        }
        if (!normalized.matches("^7\\d{8}$")) {
            throw ApiException.badRequest("Phone number must be a valid Rwanda number with optional country code +250");
        }
        return "+250" + normalized;
    }

    public static Set<String> rwandanPhoneVariants(String phoneNumber) {
        String canonical = normalizeRwandanPhoneNumber(phoneNumber);
        String local = "0" + canonical.substring(4);
        String international = canonical.substring(1);
        Set<String> variants = new LinkedHashSet<>();
        variants.add(canonical);
        variants.add(local);
        variants.add(international);
        return variants;
    }

    public static String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }
}
