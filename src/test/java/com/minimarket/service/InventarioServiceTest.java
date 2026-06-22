package com.minimarket.service;

import com.minimarket.entity.Inventario;
import com.minimarket.entity.Producto;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.impl.InventarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    @Test
    void registrarMovimiento_conDatosValidos_guardaInventario() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, "Entrada", 10);

        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventario resultado = inventarioService.registrarMovimiento(inventario);

        assertNotNull(resultado);
        assertSame(producto, resultado.getProducto());
        assertEquals("Entrada", resultado.getTipoMovimiento());
        assertEquals(10, resultado.getCantidad());
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void registrarMovimiento_conSalidaValida_guardaInventario() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, "Salida", 4);

        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventario resultado = inventarioService.registrarMovimiento(inventario);

        assertNotNull(resultado);
        assertSame(producto, resultado.getProducto());
        assertEquals("Salida", resultado.getTipoMovimiento());
        assertEquals(4, resultado.getCantidad());
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void registrarMovimiento_conTipoMovimientoConEspacios_normalizaYGuarda() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, "  Salida  ", 4);

        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventario resultado = inventarioService.registrarMovimiento(inventario);

        assertEquals("Salida", resultado.getTipoMovimiento());
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void registrarMovimiento_conTipoMovimientoEnMinuscula_guardaInventario() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, "entrada", 8);

        when(inventarioRepository.save(any(Inventario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Inventario resultado = inventarioService.registrarMovimiento(inventario);

        assertEquals("entrada", resultado.getTipoMovimiento());
        assertEquals(8, resultado.getCantidad());
        verify(inventarioRepository).save(inventario);
    }

    @Test
    void registrarMovimiento_conInventarioNulo_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(null)
        );

        assertEquals("El movimiento de inventario es obligatorio", exception.getMessage());
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void registrarMovimiento_conProductoNulo_lanzaExcepcion() {
        Inventario inventario = crearInventario(null, "Entrada", 10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(inventario)
        );

        assertEquals("El producto asociado al inventario es obligatorio", exception.getMessage());
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void registrarMovimiento_conTipoMovimientoNulo_lanzaExcepcion() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, null, 10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(inventario)
        );

        assertEquals("El tipo de movimiento es obligatorio", exception.getMessage());
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void registrarMovimiento_conTipoMovimientoVacio_lanzaExcepcion() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, "   ", 10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(inventario)
        );

        assertEquals("El tipo de movimiento es obligatorio", exception.getMessage());
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void registrarMovimiento_conTipoMovimientoInvalido_lanzaExcepcion() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, "Ajuste", 10);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(inventario)
        );

        assertEquals("El tipo de movimiento debe ser Entrada o Salida", exception.getMessage());
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void registrarMovimiento_conCantidadNula_lanzaExcepcion() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, "Entrada", null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(inventario)
        );

        assertEquals("La cantidad debe ser mayor a cero", exception.getMessage());
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void registrarMovimiento_conCantidadCero_lanzaExcepcion() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, "Entrada", 0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(inventario)
        );

        assertEquals("La cantidad debe ser mayor a cero", exception.getMessage());
        verifyNoInteractions(inventarioRepository);
    }

    @Test
    void registrarMovimiento_conCantidadNegativa_lanzaExcepcion() {
        Producto producto = crearProducto(1L, "Aceite");
        Inventario inventario = crearInventario(producto, "Salida", -5);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(inventario)
        );

        assertEquals("La cantidad debe ser mayor a cero", exception.getMessage());
        verifyNoInteractions(inventarioRepository);
    }

    private Producto crearProducto(Long id, String nombre) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(2500.0);
        producto.setStock(20);
        return producto;
    }

    private Inventario crearInventario(Producto producto, String tipoMovimiento, Integer cantidad) {
        Inventario inventario = new Inventario();
        inventario.setProducto(producto);
        inventario.setTipoMovimiento(tipoMovimiento);
        inventario.setCantidad(cantidad);
        inventario.setFechaMovimiento(new Date());
        return inventario;
    }
}