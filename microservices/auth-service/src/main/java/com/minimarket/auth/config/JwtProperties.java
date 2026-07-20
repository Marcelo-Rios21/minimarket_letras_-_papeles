package com.minimarket.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(

        String secret,

        long expirationMs,

        String issuer
) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "La propiedad app.jwt.secret es obligatoria"
            );
        }

        if (expirationMs <= 0) {
            throw new IllegalArgumentException(
                    "La propiedad app.jwt.expiration-ms debe ser mayor que cero"
            );
        }

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException(
                    "La propiedad app.jwt.issuer es obligatoria"
            );
        }
    }
}
