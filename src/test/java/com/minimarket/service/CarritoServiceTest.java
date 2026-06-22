package com.minimarket.service;

import com.minimarket.entity.Carrito;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.CarritoRepository;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.service.impl.CarritoServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarritoServiceTest {

    @Mock
    private CarritoRepository carritoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CarritoServiceImpl carritoService;

    @Test
    void agregarProducto_conStockSuficiente_guardaCarrito() {
        Usuario usuario = crearUsuario(1L, "cliente");
        Producto producto = crearProducto(1L, "Arroz", 10);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.agregarProducto(1L, 1L, 3);

        assertNotNull(resultado);
        assertSame(usuario, resultado.getUsuario());
        assertSame(producto, resultado.getProducto());
        assertEquals(3, resultado.getCantidad());
        verify(usuarioRepository).findById(1L);
        verify(productoRepository).findById(1L);
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void agregarProducto_sinStockSuficiente_lanzaExcepcion() {
        Usuario usuario = crearUsuario(1L, "cliente");
        Producto producto = crearProducto(1L, "Arroz", 2);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto(1L, 1L, 5)
        );

        assertEquals("Stock insuficiente para agregar el producto al carrito", exception.getMessage());
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    void agregarProducto_conStockIgualALaCantidad_guardaCarrito() {
        Usuario usuario = crearUsuario(1L, "cliente");
        Producto producto = crearProducto(1L, "Arroz", 5);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(carritoRepository.save(any(Carrito.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Carrito resultado = carritoService.agregarProducto(1L, 1L, 5);

        assertNotNull(resultado);
        assertEquals(5, resultado.getCantidad());
        assertSame(usuario, resultado.getUsuario());
        assertSame(producto, resultado.getProducto());
        verify(carritoRepository).save(any(Carrito.class));
    }

    @Test
    void agregarProducto_conCantidadNula_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto(1L, 1L, null)
        );

        assertEquals("La cantidad debe ser mayor a cero", exception.getMessage());
        verifyNoInteractions(usuarioRepository, productoRepository, carritoRepository);
    }

    @Test
    void agregarProducto_conCantidadCero_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto(1L, 1L, 0)
        );

        assertEquals("La cantidad debe ser mayor a cero", exception.getMessage());
        verifyNoInteractions(usuarioRepository, productoRepository, carritoRepository);
    }

    @Test
    void agregarProducto_conCantidadNegativa_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto(1L, 1L, -1)
        );

        assertEquals("La cantidad debe ser mayor a cero", exception.getMessage());
        verifyNoInteractions(usuarioRepository, productoRepository, carritoRepository);
    }

    @Test
    void agregarProducto_conUsuarioNulo_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto(null, 1L, 2)
        );

        assertEquals("El usuario es obligatorio", exception.getMessage());
        verifyNoInteractions(usuarioRepository, productoRepository, carritoRepository);
    }

    @Test
    void agregarProducto_conProductoNulo_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto(1L, null, 2)
        );

        assertEquals("El producto es obligatorio", exception.getMessage());
        verifyNoInteractions(usuarioRepository, productoRepository, carritoRepository);
    }

    @Test
    void agregarProducto_conUsuarioInexistente_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto(99L, 1L, 2)
        );

        assertEquals("Usuario no encontrado", exception.getMessage());
        verify(usuarioRepository).findById(99L);
        verifyNoInteractions(productoRepository);
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    void agregarProducto_conProductoInexistente_lanzaExcepcion() {
        Usuario usuario = crearUsuario(1L, "cliente");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto(1L, 99L, 2)
        );

        assertEquals("Producto no encontrado", exception.getMessage());
        verify(usuarioRepository).findById(1L);
        verify(productoRepository).findById(99L);
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    @Test
    void agregarProducto_conStockNulo_lanzaExcepcion() {
        Usuario usuario = crearUsuario(1L, "cliente");
        Producto producto = crearProducto(1L, "Arroz", null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> carritoService.agregarProducto(1L, 1L, 1)
        );

        assertEquals("Stock insuficiente para agregar el producto al carrito", exception.getMessage());
        verify(carritoRepository, never()).save(any(Carrito.class));
    }

    private Usuario crearUsuario(Long id, String username) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setPassword("password123");
        usuario.setNombre("Cliente");
        usuario.setApellido("Prueba");
        usuario.setEmail("cliente@minimarket.cl");
        usuario.setDireccion("Direccion de prueba");
        return usuario;
    }

    private Producto crearProducto(Long id, String nombre, Integer stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(1500.0);
        producto.setStock(stock);
        return producto;
    }
}