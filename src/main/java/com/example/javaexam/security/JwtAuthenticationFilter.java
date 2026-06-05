package com.example.javaexam.security;

import com.example.javaexam.models.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Authenticates requests bearing a valid <b>access</b> JWT.
 *
 * <p>For each request it: parses the {@code Bearer} token, rejects anything
 * that is not an access token, loads the user, and confirms the token's version
 * ({@code tv}) still matches the user's current {@code tokenVersion} — so a
 * logout-all / password change instantly invalidates outstanding access tokens.
 * Runs once per request and passes through unauthenticated when no valid token
 * is present (so {@code permitAll} endpoints still work).
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader(HEADER);
        if (authHeader == null || !authHeader.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final Claims claims;
        try {
            claims = jwtService.parseClaims(authHeader.substring(PREFIX.length()));
        } catch (Exception ex) {
            // Malformed / expired / bad signature -> treat as unauthenticated.
            filterChain.doFilter(request, response);
            return;
        }

        // Only access tokens may authenticate API calls; refresh tokens may not.
        if (!JwtService.TYPE_ACCESS.equals(claims.get(JwtService.CLAIM_TYPE, String.class))) {
            filterChain.doFilter(request, response);
            return;
        }

        final String email = claims.getSubject();
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null
                && userDetailsService.loadUserByUsername(email) instanceof User user) {

            Integer tokenVersion = claims.get(JwtService.CLAIM_TOKEN_VERSION, Integer.class);
            boolean versionMatches = tokenVersion != null && tokenVersion == user.getTokenVersion();

            if (versionMatches && user.isEnabled()) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user, null, user.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }
}
