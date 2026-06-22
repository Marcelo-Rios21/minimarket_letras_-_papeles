package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductoTest {

    @Test
    void producto_debeMantenerNombrePrecioStockYCategoria() {
        Categoria categoria = new Categoria();
        categoria.setId(1L);
        categoria.setNombre("Abarrotes");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Fideos");
        producto.setPrecio(1200.0);
        producto.setStock(30);
        producto.setCategoria(categoria);

        assertEquals(1L, producto.getId());
        assertEquals("Fideos", producto.getNombre());
        assertEquals(1200.0, producto.getPrecio());
        assertEquals(30, producto.getStock());
        assertSame(categoria, producto.getCategoria());
        assertEquals("Abarrotes", producto.getCategoria().getNombre());
    }
}