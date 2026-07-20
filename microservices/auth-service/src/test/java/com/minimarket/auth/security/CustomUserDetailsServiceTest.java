package com.minimarket.auth.security;

import com.minimarket.auth.entity.NombreRol;
import com.minimarket.auth.entity.Rol;
import com.minimarket.auth.entity.Usuario;
import com.minimarket.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(
                usuarioRepository
        );
    }

    @Test
    void cargaUsuarioActivoConRolesOrdenados() {
        Usuario usuario = crearUsuario(true);

        when(usuarioRepository.findByUsernameIgnoreCase(
                "empleado"
        )).thenReturn(Optional.of(usuario));

        UserDetails resultado =
                userDetailsService.loadUserByUsername(
                        " EmPlEaDo "
                );

        assertThat(resultado.getUsername())
                .isEqualTo("empleado");

        assertThat(resultado.getPassword())
                .isEqualTo("hash-bcrypt");

        assertThat(resultado.isEnabled()).isTrue();

        assertThat(resultado.getAuthorities())
                .extracting("authority")
                .containsExactly(
                        "ROLE_EMPLEADO",
                        "ROLE_GERENTE"
                );

        verify(usuarioRepository)
                .findByUsernameIgnoreCase("empleado");
    }

    @Test
    void rechazaUsuarioInactivo() {
        Usuario usuario = crearUsuario(false);

        when(usuarioRepository.findByUsernameIgnoreCase(
                "empleado"
        )).thenReturn(Optional.of(usuario));

        assertThatThrownBy(
                () -> userDetailsService.loadUserByUsername(
                        "empleado"
                )
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Credenciales inválidas");
    }

    @Test
    void rechazaUsernameVacioSinConsultarRepositorio() {
        assertThatThrownBy(
                () -> userDetailsService.loadUserByUsername(" ")
        )
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Credenciales inválidas");

        verifyNoInteractions(usuarioRepository);
    }

    private Usuario crearUsuario(boolean activo) {
        Usuario usuario = new Usuario();

        usuario.setUsername("empleado");
        usuario.setPassword("hash-bcrypt");
        usuario.setNombre("Ana");
        usuario.setApellido("Pérez");
        usuario.setEmail("ana@correo.cl");
        usuario.setDireccion("Sucursal Central");
        usuario.setActivo(activo);
        usuario.setRoles(
                Set.of(
                        new Rol(NombreRol.ROLE_GERENTE),
                        new Rol(NombreRol.ROLE_EMPLEADO)
                )
        );

        return usuario;
    }
}
