package com.minimarket.security.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {

        logger.warn(
                "Intento de acceso no autenticado o credenciales invalidas. Ruta: {}, IP: {}, Motivo: {}",
                request.getRequestURI(),
                request.getRemoteAddr(),
                authException.getMessage()
        );

        response.addHeader("WWW-Authenticate", "Basic realm=\"minimarket\"");
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Autenticacion requerida");
    }
}
