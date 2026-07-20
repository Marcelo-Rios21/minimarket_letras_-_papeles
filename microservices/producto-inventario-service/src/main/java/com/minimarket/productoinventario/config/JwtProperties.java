package com.minimarket.productoinventario.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(

        String secret,

        String issuer
) {

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            throw new IllegalArgumentException(
                    "La propiedad app.jwt.secret es obligatoria"
            );
        }

        if (issuer == null || issuer.isBlank()) {
            throw new IllegalArgumentException(
                    "La propiedad app.jwt.issuer es obligatoria"
            );
        }
    }
}
