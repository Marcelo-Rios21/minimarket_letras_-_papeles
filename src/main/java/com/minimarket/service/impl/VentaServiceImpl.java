package com.minimarket.service.impl;

import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Producto;
import com.minimarket.entity.Usuario;
import com.minimarket.entity.Venta;
import com.minimarket.repository.ProductoRepository;
import com.minimarket.repository.UsuarioRepository;
import com.minimarket.repository.VentaRepository;
import com.minimarket.service.UsuarioService;
import com.minimarket.service.VentaService;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    public VentaServiceImpl(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            UsuarioRepository usuarioRepository,
            UsuarioService usuarioService
    ) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @Override
    public List<Venta> findAll() {
        return ventaRepository.findAll();
    }

    @Override
    public Venta findById(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    @Override
    public Venta save(Venta venta) {
        return registrarVenta(venta);
    }

    @Override
    public Venta registrarVenta(Venta venta) {
        validarVentaBase(venta);

        Usuario usuario = buscarYValidarUsuario(venta.getUsuario().getId());

        double total = 0.0;

        for (DetalleVenta detalle : venta.getDetalles()) {
            validarDetalle(detalle);

            Long productoId = detalle.getProducto().getId();
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado: " + productoId));

            if (producto.getStock() < detalle.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente para el producto: " + producto.getNombre());
            }

            detalle.setVenta(venta);
            detalle.setProducto(producto);
            detalle.setPrecio(producto.getPrecio());

            total += producto.getPrecio() * detalle.getCantidad();

            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        venta.setUsuario(usuario);

        if (venta.getFecha() == null) {
            venta.setFecha(new Date());
        }

        venta.setTotal(total);

        return ventaRepository.save(venta);
    }

    @Override
    public List<Venta> findByUsuarioId(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId);
    }

    private void validarVentaBase(Venta venta) {
        if (venta == null) {
            throw new IllegalArgumentException("La venta no puede ser nula");
        }

        if (venta.getUsuario() == null || venta.getUsuario().getId() == null) {
            throw new IllegalArgumentException("La venta debe estar asociada a un usuario valido");
        }

        if (venta.getDetalles() == null || venta.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("La venta debe tener al menos un detalle");
        }
    }

    private Usuario buscarYValidarUsuario(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + usuarioId));

        if (!usuarioService.tieneDatosCompletos(usuario)) {
            throw new IllegalArgumentException("El usuario no tiene sus datos obligatorios completos");
        }

        if (!usuarioService.tieneRolValidoParaVentas(usuario)) {
            throw new IllegalArgumentException("El usuario no tiene un rol valido para registrar ventas");
        }

        return usuario;
    }

    private void validarDetalle(DetalleVenta detalle) {
        if (detalle == null) {
            throw new IllegalArgumentException("El detalle de venta no puede ser nulo");
        }

        if (detalle.getProducto() == null || detalle.getProducto().getId() == null) {
            throw new IllegalArgumentException("Cada detalle debe estar asociado a un producto valido");
        }

        if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }
}