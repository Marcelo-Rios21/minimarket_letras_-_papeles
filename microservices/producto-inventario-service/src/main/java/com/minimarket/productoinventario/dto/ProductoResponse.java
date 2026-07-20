package com.minimarket.productoinventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Información pública de un producto")
public record ProductoResponse(

        @Schema(example = "10")
        Long id,

        @Schema(example = "Leche entera")
        String nombre,

        @Schema(example = "1290.00")
        BigDecimal precio,

        @Schema(
                description = "Stock actual administrado por inventario",
                example = "25"
        )
        Integer stock,

        @Schema(example = "1")
        Long categoriaId,

        @Schema(example = "Lácteos")
        String categoriaNombre
) {
}
