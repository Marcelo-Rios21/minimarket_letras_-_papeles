package com.minimarket.auth.mapper;

import com.minimarket.auth.dto.UsuarioResponse;
import com.minimarket.auth.entity.Rol;
import com.minimarket.auth.entity.Usuario;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario usuario) {
        List<String> roles = usuario.getRoles()
                .stream()
                .map(Rol::getNombre)
                .map(Enum::name)
                .sorted()
                .toList();

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getEmail(),
                usuario.getDireccion(),
                usuario.isActivo(),
                roles
        );
    }
}

