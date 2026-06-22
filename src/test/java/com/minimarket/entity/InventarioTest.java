package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class InventarioTest {

    @Test
    void inventario_debeMantenerProductoTipoMovimientoCantidadYFecha() {
        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Aceite");
        producto.setPrecio(2500.0);
        producto.setStock(20);

        Date fecha = new Date();

        Inventario inventario = new Inventario();
        inventario.setId(1L);
        inventario.setProducto(producto);
        inventario.setTipoMovimiento("Entrada");
        inventario.setCantidad(15);
        inventario.setFechaMovimiento(fecha);

        assertEquals(1L, inventario.getId());
        assertSame(producto, inventario.getProducto());
        assertEquals("Entrada", inventario.getTipoMovimiento());
        assertEquals(15, inventario.getCantidad());
        assertSame(fecha, inventario.getFechaMovimiento());
        assertEquals("Aceite", inventario.getProducto().getNombre());
    }
}