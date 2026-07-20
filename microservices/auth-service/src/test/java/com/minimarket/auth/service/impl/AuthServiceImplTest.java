package com.minimarket.auth.service.impl;

import com.minimarket.auth.dto.AuthResponse;
import com.minimarket.auth.dto.LoginRequest;
import com.minimarket.auth.dto.RegisterRequest;
import com.minimarket.auth.dto.UsuarioResponse;
import com.minimarket.auth.entity.Usuario;
import com.minimarket.auth.mapper.UsuarioMapper;
import com.minimarket.auth.security.JwtService;
import com.minimarket.auth.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private UsuarioMapper usuarioMapper;

    @Mock
    private JwtService jwtService;

    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                authenticationManager,
                usuarioService,
                usuarioMapper,
                jwtService
        );
    }

    @Test
    void registrarDelegaEnServicioYDevuelveRespuestaSegura() {
        RegisterRequest request = new RegisterRequest(
                "cliente",
                "password123",
                "Jaime",
                "Carte",
                "jaime@correo.cl",
                "Calle Principal"
        );

        Usuario usuario = new Usuario();

        UsuarioResponse response = new UsuarioResponse(
                1L,
                "cliente",
                "Jaime",
                "Carte",
                "jaime@correo.cl",
                "Calle Principal",
                true,
                List.of("ROLE_CLIENTE")
        );

        when(usuarioService.registrarCliente(request))
                .thenReturn(usuario);

        when(usuarioMapper.toResponse(usuario))
                .thenReturn(response);

        UsuarioResponse resultado = authService.registrar(
                request
        );

        assertThat(resultado).isEqualTo(response);

        verify(usuarioService).registrarCliente(request);
        verify(usuarioMapper).toResponse(usuario);
    }

    @Test
    void autenticarNormalizaUsernameYDevuelveJwt() {
        LoginRequest request = new LoginRequest(
                " EmPlEaDo ",
                "password123"
        );

        UserDetails principal = User.withUsername("empleado")
                .password("hash")
                .authorities(
                        "ROLE_GERENTE",
                        "ROLE_EMPLEADO"
                )
                .build();

        Authentication autenticado =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        when(authenticationManager.authenticate(
                org.mockito.ArgumentMatchers.any(
                        Authentication.class
                )
        )).thenReturn(autenticado);

        when(jwtService.generarToken(principal))
                .thenReturn("jwt-firmado");

        when(jwtService.obtenerExpiracionSegundos())
                .thenReturn(3_600L);

        AuthResponse resultado = authService.autenticar(request);

        assertThat(resultado.token())
                .isEqualTo("jwt-firmado");

        assertThat(resultado.tokenType())
                .isEqualTo("Bearer");

        assertThat(resultado.expiresInSeconds())
                .isEqualTo(3_600L);

        assertThat(resultado.username())
                .isEqualTo("empleado");

        assertThat(resultado.roles())
                .containsExactly(
                        "ROLE_EMPLEADO",
                        "ROLE_GERENTE"
                );

        ArgumentCaptor<Authentication> captor =
                ArgumentCaptor.forClass(Authentication.class);

        verify(authenticationManager)
                .authenticate(captor.capture());

        assertThat(captor.getValue().getPrincipal())
                .isEqualTo("empleado");

        assertThat(captor.getValue().getCredentials())
                .isEqualTo("password123");

        verify(jwtService).generarToken(principal);
    }

    @Test
    void autenticarRechazaPrincipalQueNoEsUserDetails() {
        LoginRequest request = new LoginRequest(
                "empleado",
                "password123"
        );

        Authentication autenticado =
                new UsernamePasswordAuthenticationToken(
                        "empleado",
                        null,
                        List.of()
                );

        when(authenticationManager.authenticate(
                org.mockito.ArgumentMatchers.any(
                        Authentication.class
                )
        )).thenReturn(autenticado);

        assertThatThrownBy(
                () -> authService.autenticar(request)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "No fue posible obtener el usuario autenticado"
                );
    }
}
