package com.minimarket.auth.security;

import com.minimarket.auth.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Component
public class JwtService {

    private static final String ROLES_CLAIM = "roles";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;

        try {
            byte[] keyBytes = Decoders.BASE64.decode(
                    jwtProperties.secret()
            );

            this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "app.jwt.secret debe ser una clave Base64 válida "
                            + "de al menos 256 bits",
                    exception
            );
        }
    }

    public String generarToken(UserDetails userDetails) {
        Instant issuedAt = Instant.now();
        Instant expiration = issuedAt.plusMillis(
                jwtProperties.expirationMs()
        );

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuer(jwtProperties.issuer())
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .claim(ROLES_CLAIM, roles)
                .signWith(secretKey)
                .compact();
    }

    public String extraerUsername(String token) {
        return extraerClaims(token).getSubject();
    }

    public List<String> extraerRoles(String token) {
        Object rolesClaim = extraerClaims(token).get(
                ROLES_CLAIM
        );

        if (!(rolesClaim instanceof List<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .map(String::valueOf)
                .sorted()
                .toList();
    }

    public boolean esTokenValido(
            String token,
            UserDetails userDetails
    ) {
        try {
            Claims claims = extraerClaims(token);

            return claims.getSubject().equals(
                    userDetails.getUsername()
            ) && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public long obtenerExpiracionSegundos() {
        return jwtProperties.expirationMs() / 1000;
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(jwtProperties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
