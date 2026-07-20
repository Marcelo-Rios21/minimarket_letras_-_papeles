package com.minimarket.productoinventario.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Datos necesarios para crear o actualizar un producto")
public record ProductoRequest(

        @Schema(
                description = "Nombre del producto",
                example = "Leche entera"
        )
        @NotBlank(message = "El nombre del producto es obligatorio")
        @Size(
                max = 150,
                message = "El nombre del producto no puede superar 150 caracteres"
        )
        String nombre,

        @Schema(
                description = "Precio unitario del producto",
                example = "1290.00"
        )
        @NotNull(message = "El precio del producto es obligatorio")
        @DecimalMin(
                value = "0.01",
                message = "El precio debe ser mayor que cero"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "El precio debe tener máximo 10 enteros y 2 decimales"
        )
        BigDecimal precio,

        @Schema(
                description = "Identificador de la categoría",
                example = "1"
        )
        @NotNull(message = "La categoría del producto es obligatoria")
        @Positive(message = "El ID de la categoría debe ser positivo")
        Long categoriaId
) {
}
