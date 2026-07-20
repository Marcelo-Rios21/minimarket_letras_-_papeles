package com.minimarket.auth.dto;

import java.util.List;

public record UsuarioResponse(

        Long id,

        String username,

        String nombre,

        String apellido,

        String email,

        String direccion,

        boolean activo,

        List<String> roles
) {
}

