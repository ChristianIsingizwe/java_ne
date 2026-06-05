package com.example.javaexam.controllers;

import com.example.javaexam.models.domains.ApiResponse;
import com.example.javaexam.models.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example protected endpoints demonstrating how roles guard access.
 *
 * <ul>
 *   <li>{@code GET /api/me} — any authenticated user; returns their profile.</li>
 *   <li>{@code GET /api/admin/ping} — restricted to {@code ROLE_ADMIN} (by URL
 *       rule in SecurityConfig and method security here).</li>
 * </ul>
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Account", description = "Endpoints that require a valid JWT")
public class MeController {

    @GetMapping("/me")
    @Operation(summary = "Return the authenticated user's profile")
    public Map<String, Object> currentUser(@AuthenticationPrincipal User user) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("id", user.getId());
        profile.put("firstName", user.getFirstName());
        profile.put("lastName", user.getLastName());
        profile.put("email", user.getEmail());
        profile.put("role", user.getRole());
        profile.put("enabled", user.isEnabled());
        return profile;
    }

    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Admin-only endpoint (requires ROLE_ADMIN)")
    public ApiResponse adminOnly() {
        return new ApiResponse("Hello ADMIN — this endpoint requires ROLE_ADMIN.");
    }
}
