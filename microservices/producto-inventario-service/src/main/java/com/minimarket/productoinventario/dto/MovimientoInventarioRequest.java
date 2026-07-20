package com.minimarket.productoinventario.dto;

import com.minimarket.productoinventario.entity.TipoMovimiento;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Datos para registrar una entrada o salida de inventario")
public record MovimientoInventarioRequest(

        @Schema(
                description = "Tipo de movimiento",
                example = "ENTRADA"
        )
        @NotNull(message = "El tipo de movimiento es obligatorio")
        TipoMovimiento tipoMovimiento,

        @Schema(
                description = "Cantidad de unidades",
                example = "10"
        )
        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor que cero")
        Integer cantidad
) {
}
