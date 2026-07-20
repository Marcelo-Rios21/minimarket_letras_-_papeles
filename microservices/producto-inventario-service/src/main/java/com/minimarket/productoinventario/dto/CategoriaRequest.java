package com.minimarket.productoinventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Datos necesarios para crear o actualizar una categoría")
public record CategoriaRequest(

        @Schema(
                description = "Nombre de la categoría",
                example = "Lácteos"
        )
        @NotBlank(message = "El nombre de la categoría es obligatorio")
        @Size(
                max = 100,
                message = "El nombre de la categoría no puede superar 100 caracteres"
        )
        String nombre
) {
}
