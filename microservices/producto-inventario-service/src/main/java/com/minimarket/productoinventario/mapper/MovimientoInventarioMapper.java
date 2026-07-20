package com.minimarket.productoinventario.mapper;

import com.minimarket.productoinventario.dto.MovimientoInventarioResponse;
import com.minimarket.productoinventario.entity.MovimientoInventario;
import com.minimarket.productoinventario.entity.Producto;

public final class MovimientoInventarioMapper {

    private MovimientoInventarioMapper() {
    }

    public static MovimientoInventarioResponse toResponse(
            MovimientoInventario movimiento
    ) {
        Producto producto = movimiento.getProducto();

        return new MovimientoInventarioResponse(
                movimiento.getId(),
                producto.getId(),
                producto.getNombre(),
                movimiento.getTipoMovimiento(),
                movimiento.getCantidad(),
                movimiento.getFechaMovimiento()
        );
    }
}
