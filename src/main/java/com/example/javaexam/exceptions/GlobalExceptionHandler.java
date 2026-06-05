package com.example.javaexam.exceptions;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.example.javaexam.models.domains.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(violation ->
                fieldErrors.put(violation.getPropertyPath().toString(), violation.getMessage()));
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMessageNotReadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException invalidFormatException && !invalidFormatException.getPath().isEmpty()) {
            String fieldName = invalidFormatException.getPath()
                    .get(invalidFormatException.getPath().size() - 1)
                    .getFieldName();
            Class<?> targetType = invalidFormatException.getTargetType();
            if (targetType.isEnum()) {
                fieldErrors.put(fieldName, "Invalid value. Accepted values are " + java.util.Arrays.toString(targetType.getEnumConstants()));
            } else if (LocalDate.class.isAssignableFrom(targetType)) {
                fieldErrors.put(fieldName, "Invalid date format. Use yyyy-MM-dd");
            } else if (Number.class.isAssignableFrom(targetType)) {
                fieldErrors.put(fieldName, "Invalid numeric value");
            } else {
                fieldErrors.put(fieldName, "Invalid value");
            }
        } else {
            fieldErrors.put("request", "Malformed JSON request body");
        }
        return build(HttpStatus.BAD_REQUEST, "Request body could not be parsed", request, fieldErrors);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, HttpServletRequest request) {
        return build(ex.getStatus(), ex.getMessage(), request, null);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        if (ex instanceof DisabledException) {
            return build(HttpStatus.FORBIDDEN, "Account is inactive", request, null);
        }
        return build(HttpStatus.UNAUTHORIZED, "Invalid email or password", request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return build(HttpStatus.FORBIDDEN, "You are not allowed to perform this action", request, null);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        Map<String, String> errors = Map.of(ex.getName(), "Invalid value supplied");
        return build(HttpStatus.BAD_REQUEST, "Request parameter validation failed", request, errors);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation", ex);
        return build(HttpStatus.CONFLICT, "The request violates a data integrity rule", request, null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request, null);
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status,
            String message,
            HttpServletRequest request,
            Map<String, String> errors) {
        return new ErrorResponse(message, status.value(), request.getRequestURI(), errors).toResponseEntity(status);
    }
}
