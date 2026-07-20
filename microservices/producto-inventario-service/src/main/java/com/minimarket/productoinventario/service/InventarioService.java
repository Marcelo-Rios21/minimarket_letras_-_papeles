package com.minimarket.productoinventario.service;

import com.minimarket.productoinventario.entity.MovimientoInventario;
import com.minimarket.productoinventario.entity.TipoMovimiento;

import java.util.List;

public interface InventarioService {

    List<MovimientoInventario> listarPorProducto(Long productoId);

    MovimientoInventario registrarMovimiento(
            Long productoId,
            TipoMovimiento tipoMovimiento,
            Integer cantidad
    );
}
