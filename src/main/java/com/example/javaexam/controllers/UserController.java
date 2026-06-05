package com.example.javaexam.controllers;

import com.example.javaexam.config.SecuredApiErrorResponses;
import com.example.javaexam.dtos.common.StatusUpdateRequest;
import com.example.javaexam.dtos.user.CreateUserRequest;
import com.example.javaexam.dtos.user.UserResponse;
import com.example.javaexam.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users")
@SecuredApiErrorResponses
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "View the current authenticated user")
    public UserResponse me(Principal principal) {
        return userService.me(principal.getName());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all application users")
    public List<UserResponse> list() {
        return userService.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a staff or customer user")
    public UserResponse create(@Valid @RequestBody CreateUserRequest request) {
        return userService.create(request);
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate or deactivate a user")
    public UserResponse updateStatus(@PathVariable Long userId, @Valid @RequestBody StatusUpdateRequest request) {
        return userService.updateStatus(userId, request);
    }
}
