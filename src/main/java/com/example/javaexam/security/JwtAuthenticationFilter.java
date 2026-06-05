package com.example.javaexam.security;

import com.example.javaexam.models.User;
import com.example.javaexam.services.TokenRevocationService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtService jwtService;
    private final TokenRevocationService tokenRevocationService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HEADER);
        if (authHeader == null || !authHeader.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims;
        try {
            claims = jwtService.parseClaims(authHeader.substring(PREFIX.length()));
        } catch (Exception ex) {
            authenticationEntryPoint.commence(
                    request, response, new BadCredentialsException("Invalid or expired bearer token", ex));
            return;
        }

        String rawToken = authHeader.substring(PREFIX.length());
        if (tokenRevocationService.isRevoked(rawToken)) {
            authenticationEntryPoint.commence(
                    request, response, new BadCredentialsException("Bearer token has been revoked"));
            return;
        }

        String email = claims.getSubject();
        if (email != null
                && SecurityContextHolder.getContext().getAuthentication() == null
                && userDetailsService.loadUserByUsername(email) instanceof User user
                && user.isEnabled()) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    user, null, user.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        filterChain.doFilter(request, response);
    }
}
