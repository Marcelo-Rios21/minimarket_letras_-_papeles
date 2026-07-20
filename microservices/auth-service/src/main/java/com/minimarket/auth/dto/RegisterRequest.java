package com.minimarket.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(

        @NotBlank(message = "El username es obligatorio")
        @Size(
                min = 3,
                max = 50,
                message = "El username debe tener entre 3 y 50 caracteres"
        )
        @Pattern(
                regexp = "^[A-Za-z0-9._-]+$",
                message = "El username contiene caracteres no permitidos"
        )
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(
                min = 8,
                max = 72,
                message = "La contraseña debe tener entre 8 y 72 caracteres"
        )
        String password,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(
                max = 80,
                message = "El nombre no puede superar 80 caracteres"
        )
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(
                max = 80,
                message = "El apellido no puede superar 80 caracteres"
        )
        String apellido,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(
                max = 120,
                message = "El email no puede superar 120 caracteres"
        )
        String email,

        @NotBlank(message = "La dirección es obligatoria")
        @Size(
                max = 200,
                message = "La dirección no puede superar 200 caracteres"
        )
        String direccion
) {
}
