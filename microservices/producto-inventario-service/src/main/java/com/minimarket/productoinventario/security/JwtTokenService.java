package com.minimarket.productoinventario.security;

import com.minimarket.productoinventario.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.List;

@Component
public class JwtTokenService {

    private static final String ROLES_CLAIM = "roles";

    private final String issuer;
    private final SecretKey secretKey;

    public JwtTokenService(JwtProperties jwtProperties) {
        this.issuer = jwtProperties.issuer();

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

    public Authentication obtenerAutenticacion(String token) {
        Claims claims = extraerClaims(token);
        String username = claims.getSubject();

        if (username == null || username.isBlank()) {
            throw new JwtException(
                    "El token no contiene un subject válido"
            );
        }

        List<SimpleGrantedAuthority> authorities =
                extraerRoles(claims)
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        return new UsernamePasswordAuthenticationToken(
                username,
                null,
                authorities
        );
    }

    private Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .requireIssuer(issuer)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private List<String> extraerRoles(Claims claims) {
        Object rolesClaim = claims.get(ROLES_CLAIM);

        if (!(rolesClaim instanceof List<?> roles)) {
            return List.of();
        }

        return roles.stream()
                .map(String::valueOf)
                .filter(role -> role.startsWith("ROLE_"))
                .distinct()
                .sorted()
                .toList();
    }
}
