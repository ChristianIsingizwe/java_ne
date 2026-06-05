package com.example.javaexam.utils;

public final class ValidationPatterns {

    public static final String HUMAN_NAME = "^(?=.*\\p{L})[\\p{L}][\\p{L} '-]{1,99}$";
    public static final String STRONG_PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s])\\S{8,72}$";
    public static final String UUID = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$";
    public static final String JWT = "^[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+\\.[A-Za-z0-9_-]+$";

    private ValidationPatterns() {
    }
}
