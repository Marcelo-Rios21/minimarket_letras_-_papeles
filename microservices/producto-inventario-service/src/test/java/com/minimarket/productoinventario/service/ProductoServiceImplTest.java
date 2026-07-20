package com.minimarket.productoinventario.service;

import com.minimarket.productoinventario.entity.Categoria;
import com.minimarket.productoinventario.entity.Producto;
import com.minimarket.productoinventario.exception.BusinessConflictException;
import com.minimarket.productoinventario.exception.ResourceNotFoundException;
import com.minimarket.productoinventario.repository.CategoriaRepository;
import com.minimarket.productoinventario.repository.MovimientoInventarioRepository;
import com.minimarket.productoinventario.repository.ProductoRepository;
import com.minimarket.productoinventario.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoServiceImplTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MovimientoInventarioRepository movimientoRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @Test
    void listar_retornaProductosRegistrados() {
        Producto producto = crearProducto(
                1L,
                "Leche",
                "1290.00",
                10,
                crearCategoria(1L, "Lácteos")
        );

        when(productoRepository.findAll())
                .thenReturn(List.of(producto));

        List<Producto> resultado = productoService.listar();

        assertEquals(1, resultado.size());
        assertSame(producto, resultado.get(0));
        verify(productoRepository).findAll();
    }

    @Test
    void listarPorCategoria_conCategoriaExistente_retornaProductos() {
        Categoria categoria = crearCategoria(1L, "Lácteos");
        Producto producto = crearProducto(
                1L,
                "Leche",
                "1290.00",
                10,
                categoria
        );

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(
                productoRepository
                        .findByCategoriaIdOrderByNombreAsc(1L)
        ).thenReturn(List.of(producto));

        List<Producto> resultado =
                productoService.listarPorCategoria(1L);

        assertEquals(1, resultado.size());
        assertSame(producto, resultado.get(0));

        verify(productoRepository)
                .findByCategoriaIdOrderByNombreAsc(1L);
    }

    @Test
    void listarPorCategoria_conCategoriaInexistente_lanzaExcepcion() {
        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productoService.listarPorCategoria(99L)
        );

        assertEquals(
                "No existe una categoría con ID 99",
                exception.getMessage()
        );

        verify(
                productoRepository,
                never()
        ).findByCategoriaIdOrderByNombreAsc(anyLong());
    }

    @Test
    void obtenerPorId_conProductoExistente_retornaProducto() {
        Producto producto = crearProducto(
                1L,
                "Leche",
                "1290.00",
                10,
                crearCategoria(1L, "Lácteos")
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        Producto resultado = productoService.obtenerPorId(1L);

        assertSame(producto, resultado);
        verify(productoRepository).findById(1L);
    }

    @Test
    void obtenerPorId_conProductoInexistente_lanzaExcepcion() {
        when(productoRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> productoService.obtenerPorId(99L)
        );

        assertEquals(
                "No existe un producto con ID 99",
                exception.getMessage()
        );
    }

    @Test
    void crear_conDatosValidos_normalizaEInicializaStockEnCero() {
        Categoria categoria = crearCategoria(1L, "Lácteos");

        Producto producto = crearProducto(
                50L,
                "  Leche entera  ",
                "1290.00",
                500,
                crearCategoria(99L, "Categoría enviada")
        );

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.save(producto))
                .thenReturn(producto);

        Producto resultado =
                productoService.crear(producto, 1L);

        assertSame(producto, resultado);
        assertNull(resultado.getId());
        assertEquals("Leche entera", resultado.getNombre());
        assertEquals(0, resultado.getStock());
        assertSame(categoria, resultado.getCategoria());

        verify(productoRepository).save(producto);
    }

    @Test
    void crear_conProductoNulo_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.crear(null, 1L)
        );

        assertEquals(
                "El producto es obligatorio",
                exception.getMessage()
        );

        verifyNoInteractions(
                productoRepository,
                categoriaRepository,
                movimientoRepository
        );
    }

    @Test
    void crear_conPrecioCero_lanzaExcepcion() {
        Producto producto = crearProducto(
                null,
                "Leche",
                "0.00",
                0,
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.crear(producto, 1L)
        );

        assertEquals(
                "El precio del producto debe ser mayor que cero",
                exception.getMessage()
        );

        verifyNoInteractions(
                productoRepository,
                categoriaRepository,
                movimientoRepository
        );
    }

    @Test
    void crear_conPrecioConMasDeDosDecimales_lanzaExcepcion() {
        Producto producto = crearProducto(
                null,
                "Leche",
                "1290.123",
                0,
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.crear(producto, 1L)
        );

        assertEquals(
                "El precio debe tener como máximo dos decimales",
                exception.getMessage()
        );

        verifyNoInteractions(
                productoRepository,
                categoriaRepository,
                movimientoRepository
        );
    }

    @Test
    void crear_sinCategoriaId_lanzaExcepcion() {
        Producto producto = crearProducto(
                null,
                "Leche",
                "1290.00",
                0,
                null
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> productoService.crear(producto, null)
        );

        assertEquals(
                "La categoría del producto es obligatoria",
                exception.getMessage()
        );

        verifyNoInteractions(
                productoRepository,
                categoriaRepository,
                movimientoRepository
        );
    }

    @Test
    void actualizar_conDatosValidos_preservaStockExistente() {
        Categoria categoriaAnterior =
                crearCategoria(1L, "Lácteos");

        Categoria categoriaNueva =
                crearCategoria(2L, "Bebidas");

        Producto existente = crearProducto(
                1L,
                "Leche",
                "1290.00",
                25,
                categoriaAnterior
        );

        Producto cambios = crearProducto(
                null,
                "  Bebida vegetal  ",
                "2490.50",
                999,
                null
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        when(categoriaRepository.findById(2L))
                .thenReturn(Optional.of(categoriaNueva));

        when(productoRepository.save(existente))
                .thenReturn(existente);

        Producto resultado =
                productoService.actualizar(1L, cambios, 2L);

        assertSame(existente, resultado);
        assertEquals("Bebida vegetal", resultado.getNombre());
        assertEquals(
                new BigDecimal("2490.50"),
                resultado.getPrecio()
        );
        assertEquals(25, resultado.getStock());
        assertSame(categoriaNueva, resultado.getCategoria());

        verify(productoRepository).save(existente);
    }

    @Test
    void eliminar_conStockDisponible_lanzaConflicto() {
        Producto producto = crearProducto(
                1L,
                "Leche",
                "1290.00",
                10,
                crearCategoria(1L, "Lácteos")
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> productoService.eliminar(1L)
        );

        assertEquals(
                "No se puede eliminar el producto porque mantiene stock",
                exception.getMessage()
        );

        verify(
                movimientoRepository,
                never()
        ).existsByProductoId(anyLong());

        verify(productoRepository, never())
                .delete(any(Producto.class));
    }

    @Test
    void eliminar_conMovimientosAsociados_lanzaConflicto() {
        Producto producto = crearProducto(
                1L,
                "Leche",
                "1290.00",
                0,
                crearCategoria(1L, "Lácteos")
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(movimientoRepository.existsByProductoId(1L))
                .thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> productoService.eliminar(1L)
        );

        assertEquals(
                "No se puede eliminar el producto porque tiene "
                        + "movimientos de inventario asociados",
                exception.getMessage()
        );

        verify(productoRepository, never())
                .delete(any(Producto.class));
    }

    @Test
    void eliminar_sinStockNiMovimientos_eliminaProducto() {
        Producto producto = crearProducto(
                1L,
                "Leche",
                "1290.00",
                0,
                crearCategoria(1L, "Lácteos")
        );

        when(productoRepository.findById(1L))
                .thenReturn(Optional.of(producto));

        when(movimientoRepository.existsByProductoId(1L))
                .thenReturn(false);

        productoService.eliminar(1L);

        verify(productoRepository).delete(producto);
    }

    private Categoria crearCategoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        return categoria;
    }

    private Producto crearProducto(
            Long id,
            String nombre,
            String precio,
            Integer stock,
            Categoria categoria
    ) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(new BigDecimal(precio));
        producto.setStock(stock);
        producto.setCategoria(categoria);
        return producto;
    }
}
