package com.minimarket.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "El username es obligatorio")
        @Size(
                max = 50,
                message = "El username no puede superar 50 caracteres"
        )
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(
                max = 72,
                message = "La contraseña no puede superar 72 caracteres"
        )
        String password
) {
}
