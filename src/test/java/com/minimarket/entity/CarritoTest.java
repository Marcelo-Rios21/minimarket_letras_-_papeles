package com.minimarket.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CarritoTest {

    @Test
    void carrito_debeMantenerUsuarioProductoYCantidad() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");

        Producto producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Arroz");
        producto.setPrecio(1500.0);
        producto.setStock(10);

        Carrito carrito = new Carrito();
        carrito.setId(1L);
        carrito.setUsuario(usuario);
        carrito.setProducto(producto);
        carrito.setCantidad(3);

        assertEquals(1L, carrito.getId());
        assertSame(usuario, carrito.getUsuario());
        assertSame(producto, carrito.getProducto());
        assertEquals(3, carrito.getCantidad());
        assertEquals("cliente", carrito.getUsuario().getUsername());
        assertEquals("Arroz", carrito.getProducto().getNombre());
    }
}