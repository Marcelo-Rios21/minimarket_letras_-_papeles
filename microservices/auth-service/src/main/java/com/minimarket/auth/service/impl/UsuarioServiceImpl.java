package com.minimarket.auth.service.impl;

import com.minimarket.auth.dto.RegisterRequest;
import com.minimarket.auth.dto.UsuarioAdminRequest;
import com.minimarket.auth.dto.UsuarioUpdateRequest;
import com.minimarket.auth.entity.NombreRol;
import com.minimarket.auth.entity.Rol;
import com.minimarket.auth.entity.Usuario;
import com.minimarket.auth.exception.BusinessConflictException;
import com.minimarket.auth.exception.ResourceNotFoundException;
import com.minimarket.auth.repository.RolRepository;
import com.minimarket.auth.repository.UsuarioRepository;
import com.minimarket.auth.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Usuario registrarCliente(RegisterRequest request) {
        String username = normalizarUsername(request.username());

        validarUsernameDisponible(username, null);

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(
                passwordEncoder.encode(request.password())
        );
        usuario.setNombre(normalizarTexto(request.nombre()));
        usuario.setApellido(normalizarTexto(request.apellido()));
        usuario.setEmail(normalizarEmail(request.email()));
        usuario.setDireccion(normalizarTexto(request.direccion()));
        usuario.setRoles(
                Set.of(buscarRol(NombreRol.ROLE_CLIENTE))
        );
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario crearUsuario(UsuarioAdminRequest request) {
        String username = normalizarUsername(request.username());

        validarUsernameDisponible(username, null);

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(
                passwordEncoder.encode(request.password())
        );
        usuario.setNombre(normalizarTexto(request.nombre()));
        usuario.setApellido(normalizarTexto(request.apellido()));
        usuario.setEmail(normalizarEmail(request.email()));
        usuario.setDireccion(normalizarTexto(request.direccion()));
        usuario.setRoles(buscarRoles(request.roles()));
        usuario.setActivo(true);

        return usuarioRepository.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un usuario con ID " + id
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario buscarActivoPorUsername(String username) {
        Usuario usuario = usuarioRepository
                .findByUsernameIgnoreCase(
                        normalizarUsername(username)
                )
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el usuario solicitado"
                ));

        if (!usuario.isActivo()) {
            throw new BusinessConflictException(
                    "El usuario se encuentra desactivado"
            );
        }

        return usuario;
    }

    @Override
    public Usuario actualizarUsuario(
            Long id,
            UsuarioUpdateRequest request
    ) {
        Usuario usuario = buscarPorId(id);
        String username = normalizarUsername(request.username());

        validarUsernameDisponible(username, id);

        usuario.setUsername(username);
        usuario.setNombre(normalizarTexto(request.nombre()));
        usuario.setApellido(normalizarTexto(request.apellido()));
        usuario.setEmail(normalizarEmail(request.email()));
        usuario.setDireccion(normalizarTexto(request.direccion()));
        usuario.setRoles(buscarRoles(request.roles()));
        usuario.setActivo(request.activo());

        if (request.password() != null
                && !request.password().isBlank()) {
            usuario.setPassword(
                    passwordEncoder.encode(request.password())
            );
        }

        return usuarioRepository.save(usuario);
    }

    @Override
    public void desactivarUsuario(Long id) {
        Usuario usuario = buscarPorId(id);

        if (!usuario.isActivo()) {
            throw new BusinessConflictException(
                    "El usuario ya se encuentra desactivado"
            );
        }

        usuario.setActivo(false);
        usuarioRepository.save(usuario);
    }

    private Set<Rol> buscarRoles(Set<NombreRol> nombres) {
        Set<Rol> roles = new HashSet<>();

        for (NombreRol nombre : nombres) {
            roles.add(buscarRol(nombre));
        }

        return roles;
    }

    private Rol buscarRol(NombreRol nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encuentra configurado el rol "
                                + nombre.name()
                ));
    }

    private void validarUsernameDisponible(
            String username,
            Long usuarioId
    ) {
        boolean existe = usuarioId == null
                ? usuarioRepository
                        .existsByUsernameIgnoreCase(username)
                : usuarioRepository
                        .existsByUsernameIgnoreCaseAndIdNot(
                                username,
                                usuarioId
                        );

        if (existe) {
            throw new BusinessConflictException(
                    "El username ya se encuentra registrado"
            );
        }
    }

    private String normalizarUsername(String valor) {
        return valor.trim().toLowerCase();
    }

    private String normalizarEmail(String valor) {
        return valor.trim().toLowerCase();
    }

    private String normalizarTexto(String valor) {
        return valor.trim();
    }
}
