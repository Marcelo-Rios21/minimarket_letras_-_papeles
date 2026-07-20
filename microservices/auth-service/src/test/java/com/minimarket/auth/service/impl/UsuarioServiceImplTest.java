package com.minimarket.auth.service.impl;

import com.minimarket.auth.dto.RegisterRequest;
import com.minimarket.auth.dto.UsuarioAdminRequest;
import com.minimarket.auth.dto.UsuarioUpdateRequest;
import com.minimarket.auth.entity.NombreRol;
import com.minimarket.auth.entity.Rol;
import com.minimarket.auth.entity.Usuario;
import com.minimarket.auth.exception.BusinessConflictException;
import com.minimarket.auth.repository.RolRepository;
import com.minimarket.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static com.minimarket.auth.entity.NombreRol.ROLE_CLIENTE;
import static com.minimarket.auth.entity.NombreRol.ROLE_EMPLEADO;
import static com.minimarket.auth.entity.NombreRol.ROLE_GERENTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(
                usuarioRepository,
                rolRepository,
                passwordEncoder
        );
    }

    @Test
    void registrarClienteNormalizaDatosYCifraPassword() {
        RegisterRequest request = new RegisterRequest(
                "Cliente.Uno",
                "password123",
                " Jaime ",
                " Carte ",
                "JAIME@CORREO.CL",
                " Calle Principal 123 "
        );

        Rol rolCliente = new Rol(ROLE_CLIENTE);

        when(usuarioRepository.existsByUsernameIgnoreCase(
                "cliente.uno"
        )).thenReturn(false);

        when(rolRepository.findByNombre(ROLE_CLIENTE))
                .thenReturn(Optional.of(rolCliente));

        when(passwordEncoder.encode("password123"))
                .thenReturn("password-cifrada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.registrarCliente(request);

        assertThat(resultado.getUsername())
                .isEqualTo("cliente.uno");

        assertThat(resultado.getPassword())
                .isEqualTo("password-cifrada");

        assertThat(resultado.getNombre())
                .isEqualTo("Jaime");

        assertThat(resultado.getApellido())
                .isEqualTo("Carte");

        assertThat(resultado.getEmail())
                .isEqualTo("jaime@correo.cl");

        assertThat(resultado.getDireccion())
                .isEqualTo("Calle Principal 123");

        assertThat(resultado.isActivo()).isTrue();

        assertThat(resultado.getRoles())
                .containsExactly(rolCliente);

        verify(passwordEncoder).encode("password123");
        verify(usuarioRepository).save(resultado);
    }

    @Test
    void registrarClienteRechazaUsernameDuplicado() {
        RegisterRequest request = new RegisterRequest(
                "cliente",
                "password123",
                "Jaime",
                "Carte",
                "jaime@correo.cl",
                "Calle Principal"
        );

        when(usuarioRepository.existsByUsernameIgnoreCase(
                "cliente"
        )).thenReturn(true);

        assertThatThrownBy(
                () -> usuarioService.registrarCliente(request)
        )
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage(
                        "El username ya se encuentra registrado"
                );

        verify(usuarioRepository, never())
                .save(any(Usuario.class));

        verifyNoInteractions(
                rolRepository,
                passwordEncoder
        );
    }

    @Test
    void crearUsuarioAsignaLosRolesSolicitados() {
        UsuarioAdminRequest request = new UsuarioAdminRequest(
                "Empleado.Uno",
                "password123",
                "Ana",
                "Pérez",
                "ANA@CORREO.CL",
                "Sucursal Central",
                Set.of(
                        ROLE_EMPLEADO,
                        ROLE_GERENTE
                )
        );

        Rol rolEmpleado = new Rol(ROLE_EMPLEADO);
        Rol rolGerente = new Rol(ROLE_GERENTE);

        when(usuarioRepository.existsByUsernameIgnoreCase(
                "empleado.uno"
        )).thenReturn(false);

        when(rolRepository.findByNombre(ROLE_EMPLEADO))
                .thenReturn(Optional.of(rolEmpleado));

        when(rolRepository.findByNombre(ROLE_GERENTE))
                .thenReturn(Optional.of(rolGerente));

        when(passwordEncoder.encode("password123"))
                .thenReturn("password-cifrada");

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.crearUsuario(request);

        assertThat(resultado.getUsername())
                .isEqualTo("empleado.uno");

        assertThat(resultado.getRoles())
                .extracting(Rol::getNombre)
                .containsExactlyInAnyOrder(
                        ROLE_EMPLEADO,
                        ROLE_GERENTE
                );

        assertThat(resultado.isActivo()).isTrue();
    }

    @Test
    void actualizarUsuarioSinPasswordConservaHashAnterior() {
        Usuario usuario = crearUsuarioActivo();
        usuario.setPassword("hash-anterior");

        Rol rolEmpleado = new Rol(ROLE_EMPLEADO);

        UsuarioUpdateRequest request = new UsuarioUpdateRequest(
                "Empleado.Actualizado",
                "",
                "Ana",
                "Pérez",
                "ANA.NUEVA@CORREO.CL",
                "Nueva Dirección",
                Set.of(ROLE_EMPLEADO),
                true
        );

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository
                .existsByUsernameIgnoreCaseAndIdNot(
                        "empleado.actualizado",
                        1L
                ))
                .thenReturn(false);

        when(rolRepository.findByNombre(ROLE_EMPLEADO))
                .thenReturn(Optional.of(rolEmpleado));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.actualizarUsuario(
                1L,
                request
        );

        assertThat(resultado.getUsername())
                .isEqualTo("empleado.actualizado");

        assertThat(resultado.getPassword())
                .isEqualTo("hash-anterior");

        assertThat(resultado.getEmail())
                .isEqualTo("ana.nueva@correo.cl");

        verify(passwordEncoder, never())
                .encode(anyString());
    }

    @Test
    void buscarActivoPorUsernameRechazaUsuarioInactivo() {
        Usuario usuario = crearUsuarioActivo();
        usuario.setActivo(false);

        when(usuarioRepository.findByUsernameIgnoreCase(
                "empleado"
        )).thenReturn(Optional.of(usuario));

        assertThatThrownBy(
                () -> usuarioService.buscarActivoPorUsername(
                        " Empleado "
                )
        )
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage(
                        "El usuario se encuentra desactivado"
                );
    }

    @Test
    void desactivarUsuarioCambiaEstadoActivo() {
        Usuario usuario = crearUsuarioActivo();

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(usuario))
                .thenReturn(usuario);

        usuarioService.desactivarUsuario(1L);

        assertThat(usuario.isActivo()).isFalse();

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void desactivarUsuarioRechazaUsuarioYaInactivo() {
        Usuario usuario = crearUsuarioActivo();
        usuario.setActivo(false);

        when(usuarioRepository.findById(1L))
                .thenReturn(Optional.of(usuario));

        assertThatThrownBy(
                () -> usuarioService.desactivarUsuario(1L)
        )
                .isInstanceOf(BusinessConflictException.class)
                .hasMessage(
                        "El usuario ya se encuentra desactivado"
                );

        verify(usuarioRepository, never())
                .save(any(Usuario.class));
    }

    private Usuario crearUsuarioActivo() {
        Usuario usuario = new Usuario();

        usuario.setUsername("empleado");
        usuario.setPassword("hash");
        usuario.setNombre("Ana");
        usuario.setApellido("Pérez");
        usuario.setEmail("ana@correo.cl");
        usuario.setDireccion("Sucursal Central");
        usuario.setRoles(
                Set.of(new Rol(NombreRol.ROLE_EMPLEADO))
        );
        usuario.setActivo(true);

        return usuario;
    }
}
