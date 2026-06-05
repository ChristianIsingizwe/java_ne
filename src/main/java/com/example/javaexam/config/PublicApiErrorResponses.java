package com.example.javaexam.config;

import com.example.javaexam.models.domains.ErrorResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(
                responseCode = "400",
                description = "The request payload failed validation",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Authentication failed",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "The request conflicts with existing data",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "500",
                description = "An unexpected server error occurred",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
})
public @interface PublicApiErrorResponses {
}
