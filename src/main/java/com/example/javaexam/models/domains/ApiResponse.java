package com.example.javaexam.models.domains;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse<T> {

    @Schema(example = "2026-06-05T07:45:12.123")
    private final String timestamp = LocalDateTime.now().toString();

    private T data;

    @Schema(example = "Registration successful. Check your email to verify your account.")
    private String message = "";

    @Schema(example = "CREATED")
    private HttpStatus status;

    @Schema(example = "null", nullable = true)
    private Object error = null;

    public ApiResponse() {
    }

    public ApiResponse(String message, Object error, HttpStatus status) {
        this.message = message;
        this.error = error;
        this.status = status;
    }

    public ApiResponse(T data, String message, Object error, HttpStatus status) {
        this.data = data;
        this.message = message;
        this.error = error;
        this.status = status;
    }

    public ApiResponse(T data) {
        this.data = data;
    }

    public ApiResponse(String message) {
        this.message = message;
    }

    public ApiResponse(T data, HttpStatus status) {
        this.data = data;
        this.status = status;
    }

    public ApiResponse(T data, String message) {
        this.data = data;
        this.message = message;
    }

    public ApiResponse(T data, String message, HttpStatus status) {
        this.data = data;
        this.message = message;
        this.status = status;
    }

    public ResponseEntity<ApiResponse<T>> toResponseEntity() {
        assert this.status != null;
        return ResponseEntity.status(this.status).body(this);
    }
}
