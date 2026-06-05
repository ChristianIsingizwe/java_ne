package com.example.javaexam.utils;

public final class ValidationPatterns {

    public static final String PHONE = "^(?:\\+?250|0)[\\s-]*7(?:[\\s-]*\\d){8}$";
    public static final String NATIONAL_ID = "^\\d{16}$";
    public static final String PASSWORD = "^(?=.*[A-Za-z])(?=.*\\d)\\S{8,100}$";
    public static final String METER_NUMBER = "^[A-Za-z0-9][A-Za-z0-9\\-/]{2,49}$";
    public static final String BILL_REFERENCE = "^[A-Za-z0-9-]{5,80}$";

    private ValidationPatterns() {
    }
}
