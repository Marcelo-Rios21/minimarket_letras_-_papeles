package com.minimarket.productoinventario.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenService jwtTokenService;

    public JwtAuthenticationFilter(
            JwtTokenService jwtTokenService
    ) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(
                "Authorization"
        );

        if (authorization == null
                || !authorization.startsWith(BEARER_PREFIX)
                || SecurityContextHolder
                        .getContext()
                        .getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization
                .substring(BEARER_PREFIX.length())
                .trim();

        if (token.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            Authentication authentication =
                    jwtTokenService.obtenerAutenticacion(token);

            if (authentication
                    instanceof UsernamePasswordAuthenticationToken
                    usernameAuthentication) {
                usernameAuthentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );
            }

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);
        } catch (
                JwtException
                | IllegalArgumentException exception
        ) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
