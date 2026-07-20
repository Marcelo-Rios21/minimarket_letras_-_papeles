package com.minimarket.productoinventario.dto;

import com.minimarket.productoinventario.entity.TipoMovimiento;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Movimiento registrado en el historial de inventario")
public record MovimientoInventarioResponse(

        @Schema(example = "25")
        Long id,

        @Schema(example = "1")
        Long productoId,

        @Schema(example = "Leche entera")
        String productoNombre,

        @Schema(example = "ENTRADA")
        TipoMovimiento tipoMovimiento,

        @Schema(example = "10")
        Integer cantidad,

        @Schema(example = "2026-07-19T10:30:00")
        LocalDateTime fechaMovimiento
) {
}
