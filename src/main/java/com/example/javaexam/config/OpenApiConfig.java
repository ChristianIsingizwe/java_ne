package com.example.javaexam.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI / Swagger configuration.
 *
 * <p>Declares API metadata and a global HTTP "bearer" (JWT) security scheme so
 * the <b>Authorize</b> button in Swagger UI lets you paste a token once and
 * call the protected endpoints. Obtain a token from
 * {@code POST /api/auth/login}, click <b>Authorize</b>, and paste it (without
 * the {@code Bearer } prefix).
 *
 * <p>Swagger UI: {@code /swagger-ui.html} &nbsp;|&nbsp;
 * OpenAPI JSON: {@code /v3/api-docs}
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI apiDocumentation() {
        return new OpenAPI()
                .info(new Info()
                        .title("Java Exam API")
                        .description("REST API with JWT authentication, role-based access, "
                                + "and email verification.")
                        .version("v1")
                        .contact(new Contact().name("Java Exam")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Paste the JWT returned by /api/auth/login")));
    }
}
