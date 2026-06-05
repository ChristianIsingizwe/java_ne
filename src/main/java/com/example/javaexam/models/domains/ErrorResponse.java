package com.example.javaexam.models.domains;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
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

    public ErrorResponse(String message) {
        this.message = message;
    }

    public ResponseEntity<ErrorResponse> toResponseEntity(HttpStatusCode status) {
        assert this.message != null;
        return ResponseEntity.status(status).body(this);
    }
}
