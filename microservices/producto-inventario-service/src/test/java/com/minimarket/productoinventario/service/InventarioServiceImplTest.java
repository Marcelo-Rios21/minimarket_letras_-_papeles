package com.minimarket.productoinventario.service;

import com.minimarket.productoinventario.entity.MovimientoInventario;
import com.minimarket.productoinventario.entity.Producto;
import com.minimarket.productoinventario.entity.TipoMovimiento;
import com.minimarket.productoinventario.exception.BusinessConflictException;
import com.minimarket.productoinventario.exception.ResourceNotFoundException;
import com.minimarket.productoinventario.repository.MovimientoInventarioRepository;
import com.minimarket.productoinventario.repository.ProductoRepository;
import com.minimarket.productoinventario.service.impl.InventarioServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @InjectMocks
    private InventarioServiceImpl inventarioService;

    @Test
    void listarPorProducto_conProductoExistente_retornaHistorial() {
        Producto producto = crearProducto(1L, 10);
        MovimientoInventario movimiento = new MovimientoInventario();

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(
                movimientoRepository
                        .findByProductoIdOrderByFechaMovimientoDesc(1L)
        ).thenReturn(List.of(movimiento));

        List<MovimientoInventario> resultado =
                inventarioService.listarPorProducto(1L);

        assertEquals(1, resultado.size());
        assertSame(movimiento, resultado.get(0));

        verify(movimientoRepository)
                .findByProductoIdOrderByFechaMovimientoDesc(1L);
    }

    @Test
    void listarPorProducto_conIdNulo_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.listarPorProducto(null)
        );

        assertEquals(
                "El ID del producto es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(
                productoRepository,
                movimientoRepository
        );
    }

    @Test
    void listarPorProducto_conProductoInexistente_lanzaExcepcion() {
        when(productoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> inventarioService.listarPorProducto(99L)
        );

        assertEquals(
                "No existe un producto con ID 99",
                exception.getMessage()
        );

        verify(
                movimientoRepository,
                never()
        ).findByProductoIdOrderByFechaMovimientoDesc(99L);
    }

    @Test
    void registrarEntrada_aumentaStockYGuardaMovimiento() {
        Producto producto = crearProducto(1L, 10);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(
                movimientoRepository.save(
                        any(MovimientoInventario.class)
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        MovimientoInventario resultado =
                inventarioService.registrarMovimiento(
                        1L,
                        TipoMovimiento.ENTRADA,
                        5
                );

        assertEquals(15, producto.getStock());
        assertSame(producto, resultado.getProducto());
        assertEquals(5, resultado.getCantidad());
        assertEquals(
                TipoMovimiento.ENTRADA,
                resultado.getTipoMovimiento()
        );

        InOrder orden = inOrder(
                productoRepository,
                movimientoRepository
        );

        orden.verify(productoRepository).save(producto);
        orden.verify(movimientoRepository).save(resultado);
    }

    @Test
    void registrarEntrada_conStockNulo_loConsideraCero() {
        Producto producto = crearProducto(1L, null);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(
                movimientoRepository.save(
                        any(MovimientoInventario.class)
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        inventarioService.registrarMovimiento(
                1L,
                TipoMovimiento.ENTRADA,
                7
        );

        assertEquals(7, producto.getStock());
        verify(productoRepository).save(producto);
    }

    @Test
    void registrarSalida_validaDisminuyeStockYGuardaMovimiento() {
        Producto producto = crearProducto(1L, 10);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(
                movimientoRepository.save(
                        any(MovimientoInventario.class)
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        MovimientoInventario resultado =
                inventarioService.registrarMovimiento(
                        1L,
                        TipoMovimiento.SALIDA,
                        4
                );

        assertEquals(6, producto.getStock());
        assertEquals(4, resultado.getCantidad());
        assertEquals(
                TipoMovimiento.SALIDA,
                resultado.getTipoMovimiento()
        );

        verify(productoRepository).save(producto);
        verify(movimientoRepository).save(resultado);
    }

    @Test
    void registrarSalida_conStockInsuficiente_lanzaConflicto() {
        Producto producto = crearProducto(1L, 3);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> inventarioService.registrarMovimiento(
                        1L,
                        TipoMovimiento.SALIDA,
                        5
                )
        );

        assertEquals(
                "Stock insuficiente para el producto 1. "
                        + "Stock disponible: 3",
                exception.getMessage()
        );

        assertEquals(3, producto.getStock());

        verify(productoRepository, never())
                .save(any(Producto.class));

        verify(movimientoRepository, never())
                .save(any(MovimientoInventario.class));
    }

    @Test
    void registrarMovimiento_conTipoNulo_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(
                        1L,
                        null,
                        5
                )
        );

        assertEquals(
                "El tipo de movimiento es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(
                productoRepository,
                movimientoRepository
        );
    }

    @Test
    void registrarMovimiento_conCantidadInvalida_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> inventarioService.registrarMovimiento(
                        1L,
                        TipoMovimiento.ENTRADA,
                        0
                )
        );

        assertEquals(
                "La cantidad debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(
                productoRepository,
                movimientoRepository
        );
    }

    @Test
    void registrarMovimiento_conProductoInexistente_lanzaExcepcion() {
        when(productoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> inventarioService.registrarMovimiento(
                        99L,
                        TipoMovimiento.ENTRADA,
                        5
                )
        );

        assertEquals(
                "No existe un producto con ID 99",
                exception.getMessage()
        );

        verify(productoRepository, never())
                .save(any(Producto.class));

        verify(movimientoRepository, never())
                .save(any(MovimientoInventario.class));
    }

    @Test
    void registrarEntrada_conDesbordamiento_lanzaConflicto() {
        Producto producto =
                crearProducto(1L, Integer.MAX_VALUE);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> inventarioService.registrarMovimiento(
                        1L,
                        TipoMovimiento.ENTRADA,
                        1
                )
        );

        assertEquals(
                "La entrada excede el stock máximo permitido",
                exception.getMessage()
        );

        assertEquals(Integer.MAX_VALUE, producto.getStock());

        verify(productoRepository, never())
                .save(any(Producto.class));

        verify(movimientoRepository, never())
                .save(any(MovimientoInventario.class));
    }

    @Test
    void registrarMovimiento_construyeMovimientoConDatosCorrectos() {
        Producto producto = crearProducto(1L, 20);

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(
                movimientoRepository.save(
                        any(MovimientoInventario.class)
                )
        ).thenAnswer(invocation -> invocation.getArgument(0));

        inventarioService.registrarMovimiento(
                1L,
                TipoMovimiento.SALIDA,
                2
        );

        ArgumentCaptor<MovimientoInventario> captor =
                ArgumentCaptor.forClass(
                        MovimientoInventario.class
                );

        verify(movimientoRepository).save(captor.capture());

        MovimientoInventario movimiento = captor.getValue();

        assertSame(producto, movimiento.getProducto());
        assertEquals(2, movimiento.getCantidad());
        assertEquals(
                TipoMovimiento.SALIDA,
                movimiento.getTipoMovimiento()
        );
    }

    private Producto crearProducto(Long id, Integer stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre("Producto de prueba");
        producto.setStock(stock);
        return producto;
    }
}
