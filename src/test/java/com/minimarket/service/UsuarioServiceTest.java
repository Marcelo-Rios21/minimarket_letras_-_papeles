package com.minimarket.service;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    @Test
    void findAll_debeRetornarUsuarios() {
        Usuario usuario = crearUsuarioCompleto();
        when(usuarioRepository.findAll()).thenReturn(List.of(usuario));

        List<Usuario> resultado = usuarioService.findAll();

        assertEquals(1, resultado.size());
        verify(usuarioRepository).findAll();
    }

    @Test
    void findById_conUsuarioExistente_debeRetornarUsuario() {
        Usuario usuario = crearUsuarioCompleto();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("cliente", resultado.get().getUsername());
        verify(usuarioRepository).findById(1L);
    }

    @Test
    void findByUsername_conUsuarioExistente_debeRetornarUsuario() {
        Usuario usuario = crearUsuarioCompleto();
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.findByUsername("cliente");

        assertTrue(resultado.isPresent());
        assertEquals("cliente", resultado.get().getUsername());
        verify(usuarioRepository).findByUsername("cliente");
    }

    @Test
    void deleteById_debeInvocarRepositorio() {
        usuarioService.deleteById(1L);

        verify(usuarioRepository).deleteById(1L);
    }

    @Test
    void tieneDatosCompletos_conUsuarioCompleto_debeRetornarTrue() {
        Usuario usuario = crearUsuarioCompleto();

        boolean resultado = usuarioService.tieneDatosCompletos(usuario);

        assertTrue(resultado);
    }

    @Test
    void tieneDatosCompletos_conUsuarioNulo_debeRetornarFalse() {
        boolean resultado = usuarioService.tieneDatosCompletos(null);

        assertFalse(resultado);
    }

    @Test
    void tieneDatosCompletos_conEmailVacio_debeRetornarFalse() {
        Usuario usuario = crearUsuarioCompleto();
        usuario.setEmail("");

        boolean resultado = usuarioService.tieneDatosCompletos(usuario);

        assertFalse(resultado);
    }

    @Test
    void tieneDatosCompletos_conDireccionNula_debeRetornarFalse() {
        Usuario usuario = crearUsuarioCompleto();
        usuario.setDireccion(null);

        boolean resultado = usuarioService.tieneDatosCompletos(usuario);

        assertFalse(resultado);
    }

    @Test
    void tieneRolValidoParaVentas_conRolCliente_debeRetornarTrue() {
        Usuario usuario = crearUsuarioCompleto();
        usuario.setRoles(Set.of(crearRol("ROLE_CLIENTE")));

        boolean resultado = usuarioService.tieneRolValidoParaVentas(usuario);

        assertTrue(resultado);
    }

    @Test
    void tieneRolValidoParaVentas_conRolEmpleado_debeRetornarTrue() {
        Usuario usuario = crearUsuarioCompleto();
        usuario.setRoles(Set.of(crearRol("ROLE_EMPLEADO")));

        boolean resultado = usuarioService.tieneRolValidoParaVentas(usuario);

        assertTrue(resultado);
    }

    @Test
    void tieneRolValidoParaVentas_conRolInvalido_debeRetornarFalse() {
        Usuario usuario = crearUsuarioCompleto();
        usuario.setRoles(Set.of(crearRol("ROLE_VISITANTE")));

        boolean resultado = usuarioService.tieneRolValidoParaVentas(usuario);

        assertFalse(resultado);
    }

    @Test
    void tieneRolValidoParaVentas_conRolesVacios_debeRetornarFalse() {
        Usuario usuario = crearUsuarioCompleto();
        usuario.setRoles(Set.of());

        boolean resultado = usuarioService.tieneRolValidoParaVentas(usuario);

        assertFalse(resultado);
    }

    @Test
    void tieneRolValidoParaVentas_conUsuarioNulo_debeRetornarFalse() {
        boolean resultado = usuarioService.tieneRolValidoParaVentas(null);

        assertFalse(resultado);
    }

    @Test
    void save_debeCodificarPasswordAntesDeGuardar() {
        Usuario usuario = crearUsuarioCompleto();
        usuario.setPassword("clave123");

        when(passwordEncoder.encode("clave123")).thenReturn("clave-codificada");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.save(usuario);

        assertEquals("clave-codificada", resultado.getPassword());
        verify(passwordEncoder).encode("clave123");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void save_conPasswordVacia_noDebeCodificarPassword() {
        Usuario usuario = crearUsuarioCompleto();
        usuario.setPassword("");

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.save(usuario);

        assertEquals("", resultado.getPassword());
        verify(passwordEncoder, never()).encode(anyString());
        verify(usuarioRepository).save(usuario);
    }

    private Usuario crearUsuarioCompleto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");
        usuario.setPassword("cliente123");
        usuario.setNombre("Cliente");
        usuario.setApellido("Demo");
        usuario.setEmail("cliente@minimarket.cl");
        usuario.setDireccion("Av. Cliente 123");
        return usuario;
    }

    private Rol crearRol(String nombreRol) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        return rol;
    }
}