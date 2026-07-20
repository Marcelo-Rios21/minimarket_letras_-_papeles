package com.minimarket.productoinventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información pública de una categoría")
public record CategoriaResponse(

        @Schema(example = "1")
        Long id,

        @Schema(example = "Lácteos")
        String nombre
) {
}
