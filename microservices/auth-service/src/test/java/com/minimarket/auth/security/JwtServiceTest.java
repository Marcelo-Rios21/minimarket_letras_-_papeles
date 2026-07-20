package com.minimarket.auth.security;

import com.minimarket.auth.config.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "VGhpcy1pcy1hLXRlc3Qta2V5LWZvci1hdXRoLXNlcnZpY2UtMTIzNDU2";

    private JwtService jwtService;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties(
                SECRET,
                3_600_000,
                "minimarket-auth-service"
        );

        jwtService = new JwtService(properties);

        userDetails = User.withUsername("empleado")
                .password("hash")
                .authorities(
                        "ROLE_GERENTE",
                        "ROLE_EMPLEADO"
                )
                .build();
    }

    @Test
    void generaTokenConUsernameYRolesOrdenados() {
        String token = jwtService.generarToken(userDetails);

        assertThat(token).isNotBlank();

        assertThat(jwtService.extraerUsername(token))
                .isEqualTo("empleado");

        assertThat(jwtService.extraerRoles(token))
                .containsExactly(
                        "ROLE_EMPLEADO",
                        "ROLE_GERENTE"
                );
    }

    @Test
    void validaTokenParaElUsuarioCorrecto() {
        String token = jwtService.generarToken(userDetails);

        assertThat(
                jwtService.esTokenValido(token, userDetails)
        ).isTrue();
    }

    @Test
    void rechazaTokenCuandoElUsuarioEsDistinto() {
        String token = jwtService.generarToken(userDetails);

        UserDetails otroUsuario = User.withUsername("gerente")
                .password("hash")
                .authorities("ROLE_GERENTE")
                .build();

        assertThat(
                jwtService.esTokenValido(token, otroUsuario)
        ).isFalse();
    }

    @Test
    void rechazaTokenMalformadoEInformaExpiracion() {
        assertThat(
                jwtService.esTokenValido(
                        "token-invalido",
                        userDetails
                )
        ).isFalse();

        assertThat(jwtService.obtenerExpiracionSegundos())
                .isEqualTo(3_600);
    }
}
