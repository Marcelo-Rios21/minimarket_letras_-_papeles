package com.minimarket.productoinventario.service.impl;

import com.minimarket.productoinventario.entity.MovimientoInventario;
import com.minimarket.productoinventario.entity.Producto;
import com.minimarket.productoinventario.entity.TipoMovimiento;
import com.minimarket.productoinventario.exception.BusinessConflictException;
import com.minimarket.productoinventario.exception.ResourceNotFoundException;
import com.minimarket.productoinventario.repository.MovimientoInventarioRepository;
import com.minimarket.productoinventario.repository.ProductoRepository;
import com.minimarket.productoinventario.service.InventarioService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class InventarioServiceImpl implements InventarioService {

    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public InventarioServiceImpl(
            ProductoRepository productoRepository,
            MovimientoInventarioRepository movimientoRepository
    ) {
        this.productoRepository = productoRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    public List<MovimientoInventario> listarPorProducto(
            Long productoId
    ) {
        obtenerProducto(productoId);

        return movimientoRepository
                .findByProductoIdOrderByFechaMovimientoDesc(
                        productoId
                );
    }

    @Override
    @Transactional
    public MovimientoInventario registrarMovimiento(
            Long productoId,
            TipoMovimiento tipoMovimiento,
            Integer cantidad
    ) {
        validarMovimiento(tipoMovimiento, cantidad);

        Producto producto = obtenerProducto(productoId);

        int stockActual = producto.getStock() == null
                ? 0
                : producto.getStock();

        int nuevoStock = calcularNuevoStock(
                producto,
                stockActual,
                tipoMovimiento,
                cantidad
        );

        producto.setStock(nuevoStock);
        productoRepository.save(producto);

        MovimientoInventario movimiento =
                new MovimientoInventario();

        movimiento.setProducto(producto);
        movimiento.setTipoMovimiento(tipoMovimiento);
        movimiento.setCantidad(cantidad);

        return movimientoRepository.save(movimiento);
    }

    private Producto obtenerProducto(Long productoId) {
        if (productoId == null) {
            throw new IllegalArgumentException(
                    "El ID del producto es obligatorio"
            );
        }

        return productoRepository.findById(productoId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No existe un producto con ID "
                                        + productoId
                        )
                );
    }

    private void validarMovimiento(
            TipoMovimiento tipoMovimiento,
            Integer cantidad
    ) {
        if (tipoMovimiento == null) {
            throw new IllegalArgumentException(
                    "El tipo de movimiento es obligatorio"
            );
        }

        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero"
            );
        }
    }

    private int calcularNuevoStock(
            Producto producto,
            int stockActual,
            TipoMovimiento tipoMovimiento,
            int cantidad
    ) {
        if (tipoMovimiento == TipoMovimiento.SALIDA) {
            if (cantidad > stockActual) {
                throw new BusinessConflictException(
                        "Stock insuficiente para el producto "
                                + producto.getId()
                                + ". Stock disponible: "
                                + stockActual
                );
            }

            return stockActual - cantidad;
        }

        try {
            return Math.addExact(stockActual, cantidad);
        } catch (ArithmeticException exception) {
            throw new BusinessConflictException(
                    "La entrada excede el stock máximo permitido"
            );
        }
    }
}
