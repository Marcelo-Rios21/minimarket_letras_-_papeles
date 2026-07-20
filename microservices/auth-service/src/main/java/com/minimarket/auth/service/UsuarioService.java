package com.minimarket.auth.service;

import com.minimarket.auth.dto.RegisterRequest;
import com.minimarket.auth.dto.UsuarioAdminRequest;
import com.minimarket.auth.dto.UsuarioUpdateRequest;
import com.minimarket.auth.entity.Usuario;

import java.util.List;

public interface UsuarioService {

    Usuario registrarCliente(RegisterRequest request);

    Usuario crearUsuario(UsuarioAdminRequest request);

    List<Usuario> listarUsuarios();

    Usuario buscarPorId(Long id);

    Usuario buscarActivoPorUsername(String username);

    Usuario actualizarUsuario(
            Long id,
            UsuarioUpdateRequest request
    );

    void desactivarUsuario(Long id);
}
