package com.example.javaexam.models.domains;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorResponse {

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(example = "2026-06-05T07:45:12.123")
    private final String timestamp = LocalDateTime.now().toString();

    @Schema(example = "Validation failed: {email=Email must be a valid address}")
    private String message = "";

    @Schema(example = "400")
    private int status;

    @Schema(example = "/api/auth/signup")
    private String path;

    private Map<String, String> errors;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public ErrorResponse(String message, int status, String path, Map<String, String> errors) {
        this.message = message;
        this.status = status;
        this.path = path;
        this.errors = errors;
    }

    public ResponseEntity<ErrorResponse> toResponseEntity(HttpStatusCode status) {
        this.status = status.value();
        return ResponseEntity.status(status).body(this);
    }
}
