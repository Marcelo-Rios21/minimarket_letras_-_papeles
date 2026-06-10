package com.minimarket.service;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.impl.VentaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private VentaServiceImpl ventaService;

    @Test
    void findAll_debeRetornarVentas() {
        Venta venta = crearVenta(crearUsuarioValido(), List.of(crearDetalle(crearProducto(10L, "Arroz", 1500.0, 10), 1)));
        when(ventaRepository.findAll()).thenReturn(List.of(venta));

        List<Venta> resultado = ventaService.findAll();

        assertEquals(1, resultado.size());
        verify(ventaRepository).findAll();
    }

    @Test
    void findById_conVentaExistente_debeRetornarVenta() {
        Venta venta = crearVenta(crearUsuarioValido(), List.of(crearDetalle(crearProducto(10L, "Arroz", 1500.0, 10), 1)));
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(venta));

        Venta resultado = ventaService.findById(1L);

        assertNotNull(resultado);
        verify(ventaRepository).findById(1L);
    }

    @Test
    void findById_conVentaInexistente_debeRetornarNull() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.empty());

        Venta resultado = ventaService.findById(1L);

        assertNull(resultado);
        verify(ventaRepository).findById(1L);
    }

    @Test
    void findByUsuarioId_debeRetornarVentasDelUsuario() {
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(new Venta()));

        List<Venta> resultado = ventaService.findByUsuarioId(1L);

        assertEquals(1, resultado.size());
        verify(ventaRepository).findByUsuarioId(1L);
    }

    @Test
    void save_debeDelegarEnRegistrarVenta() {
        Usuario usuario = crearUsuarioValido();
        Producto producto = crearProducto(10L, "Arroz", 1500.0, 10);
        DetalleVenta detalle = crearDetalle(producto, 1);
        Venta venta = crearVenta(usuario, List.of(detalle));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneDatosCompletos(usuario)).thenReturn(true);
        when(usuarioService.tieneRolValidoParaVentas(usuario)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Venta resultado = ventaService.save(venta);

        assertEquals(1500.0, resultado.getTotal());
        verify(ventaRepository).save(venta);
    }

    @Test
    void registrarVenta_conStockSuficiente_debeGuardarVentaYCalcularTotal() {
        Usuario usuario = crearUsuarioValido();
        Producto producto = crearProducto(10L, "Arroz", 1500.0, 10);
        DetalleVenta detalle = crearDetalle(producto, 2);
        Venta venta = crearVenta(usuario, List.of(detalle));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneDatosCompletos(usuario)).thenReturn(true);
        when(usuarioService.tieneRolValidoParaVentas(usuario)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Venta resultado = ventaService.registrarVenta(venta);

        assertEquals(3000.0, resultado.getTotal());
        assertEquals(8, producto.getStock());
        assertEquals(1500.0, detalle.getPrecio());
        assertSame(venta, detalle.getVenta());

        verify(productoRepository).save(producto);
        verify(ventaRepository).save(venta);
    }

    @Test
    void registrarVenta_conFechaNula_debeAsignarFechaAutomaticamente() {
        Usuario usuario = crearUsuarioValido();
        Producto producto = crearProducto(10L, "Arroz", 1500.0, 10);
        DetalleVenta detalle = crearDetalle(producto, 1);
        Venta venta = crearVenta(usuario, List.of(detalle));
        venta.setFecha(null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneDatosCompletos(usuario)).thenReturn(true);
        when(usuarioService.tieneRolValidoParaVentas(usuario)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Venta resultado = ventaService.registrarVenta(venta);

        assertNotNull(resultado.getFecha());
        assertEquals(1500.0, resultado.getTotal());
    }

    @Test
    void registrarVenta_sinStockSuficiente_debeLanzarExcepcion() {
        Usuario usuario = crearUsuarioValido();
        Producto producto = crearProducto(10L, "Arroz", 1500.0, 1);
        DetalleVenta detalle = crearDetalle(producto, 2);
        Venta venta = crearVenta(usuario, List.of(detalle));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneDatosCompletos(usuario)).thenReturn(true);
        when(usuarioService.tieneRolValidoParaVentas(usuario)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(venta)
        );

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conProductoInexistente_debeLanzarExcepcion() {
        Usuario usuario = crearUsuarioValido();
        Producto producto = crearProducto(10L, "Arroz", 1500.0, 10);
        DetalleVenta detalle = crearDetalle(producto, 1);
        Venta venta = crearVenta(usuario, List.of(detalle));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneDatosCompletos(usuario)).thenReturn(true);
        when(usuarioService.tieneRolValidoParaVentas(usuario)).thenReturn(true);
        when(productoRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(venta)
        );

        assertTrue(ex.getMessage().contains("Producto no encontrado"));
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conUsuarioInexistente_debeLanzarExcepcion() {
        Usuario usuario = crearUsuarioValido();
        Producto producto = crearProducto(10L, "Arroz", 1500.0, 10);
        DetalleVenta detalle = crearDetalle(producto, 1);
        Venta venta = crearVenta(usuario, List.of(detalle));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(venta)
        );

        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
        verifyNoInteractions(productoRepository);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conUsuarioIncompleto_debeLanzarExcepcion() {
        Usuario usuario = crearUsuarioValido();
        Producto producto = crearProducto(10L, "Arroz", 1500.0, 10);
        DetalleVenta detalle = crearDetalle(producto, 1);
        Venta venta = crearVenta(usuario, List.of(detalle));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneDatosCompletos(usuario)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(venta)
        );

        assertTrue(ex.getMessage().contains("datos obligatorios completos"));
        verifyNoInteractions(productoRepository);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conRolInvalido_debeLanzarExcepcion() {
        Usuario usuario = crearUsuarioValido();
        Producto producto = crearProducto(10L, "Arroz", 1500.0, 10);
        DetalleVenta detalle = crearDetalle(producto, 1);
        Venta venta = crearVenta(usuario, List.of(detalle));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneDatosCompletos(usuario)).thenReturn(true);
        when(usuarioService.tieneRolValidoParaVentas(usuario)).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(venta)
        );

        assertTrue(ex.getMessage().contains("rol valido"));
        verifyNoInteractions(productoRepository);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conVentaNula_debeLanzarExcepcion() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(null)
        );

        assertTrue(ex.getMessage().contains("no puede ser nula"));
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(productoRepository);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_sinUsuario_debeLanzarExcepcion() {
        Venta venta = new Venta();
        venta.setDetalles(List.of(crearDetalle(crearProducto(10L, "Arroz", 1500.0, 10), 1)));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(venta)
        );

        assertTrue(ex.getMessage().contains("usuario valido"));
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(productoRepository);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_sinDetalles_debeLanzarExcepcion() {
        Usuario usuario = crearUsuarioValido();
        Venta venta = crearVenta(usuario, List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(venta)
        );

        assertTrue(ex.getMessage().contains("al menos un detalle"));
        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(productoRepository);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conDetalleNulo_debeLanzarExcepcion() {
        Usuario usuario = crearUsuarioValido();
        Venta venta = crearVenta(usuario, java.util.Arrays.asList((DetalleVenta) null));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneDatosCompletos(usuario)).thenReturn(true);
        when(usuarioService.tieneRolValidoParaVentas(usuario)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(venta)
        );

        assertTrue(ex.getMessage().contains("detalle de venta"));
        verifyNoInteractions(productoRepository);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    @Test
    void registrarVenta_conCantidadCero_debeLanzarExcepcion() {
        Usuario usuario = crearUsuarioValido();
        Producto producto = crearProducto(10L, "Arroz", 1500.0, 10);
        DetalleVenta detalle = crearDetalle(producto, 0);
        Venta venta = crearVenta(usuario, List.of(detalle));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioService.tieneDatosCompletos(usuario)).thenReturn(true);
        when(usuarioService.tieneRolValidoParaVentas(usuario)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.registrarVenta(venta)
        );

        assertTrue(ex.getMessage().contains("mayor a cero"));
        verifyNoInteractions(productoRepository);
        verify(ventaRepository, never()).save(any(Venta.class));
    }

    private Venta crearVenta(Usuario usuario, List<DetalleVenta> detalles) {
        Venta venta = new Venta();
        venta.setUsuario(usuario);
        venta.setFecha(new Date());
        venta.setDetalles(detalles);
        return venta;
    }

    private Usuario crearUsuarioValido() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("cliente");
        usuario.setNombre("Cliente");
        usuario.setApellido("Demo");
        usuario.setEmail("cliente@minimarket.cl");
        usuario.setDireccion("Av. Cliente 123");
        usuario.setRoles(Set.of(crearRol("ROLE_CLIENTE")));
        return usuario;
    }

    private Rol crearRol(String nombreRol) {
        Rol rol = new Rol();
        rol.setNombre(nombreRol);
        return rol;
    }

    private Producto crearProducto(Long id, String nombre, Double precio, Integer stock) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        return producto;
    }

    private DetalleVenta crearDetalle(Producto producto, Integer cantidad) {
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        return detalle;
    }
}