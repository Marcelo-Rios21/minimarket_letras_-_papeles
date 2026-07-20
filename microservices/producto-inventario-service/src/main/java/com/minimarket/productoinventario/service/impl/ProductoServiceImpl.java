package com.minimarket.productoinventario.service.impl;

import com.minimarket.productoinventario.entity.Categoria;
import com.minimarket.productoinventario.entity.Producto;
import com.minimarket.productoinventario.exception.BusinessConflictException;
import com.minimarket.productoinventario.exception.ResourceNotFoundException;
import com.minimarket.productoinventario.repository.CategoriaRepository;
import com.minimarket.productoinventario.repository.MovimientoInventarioRepository;
import com.minimarket.productoinventario.repository.ProductoRepository;
import com.minimarket.productoinventario.service.ProductoService;
import com.minimarket.productoinventario.validation.InputValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MovimientoInventarioRepository movimientoRepository;

    public ProductoServiceImpl(
            ProductoRepository productoRepository,
            CategoriaRepository categoriaRepository,
            MovimientoInventarioRepository movimientoRepository
    ) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.movimientoRepository = movimientoRepository;
    }

    @Override
    public List<Producto> listar() {
        return productoRepository.findAll();
    }

    @Override
    public List<Producto> listarPorCategoria(Long categoriaId) {
        obtenerCategoria(categoriaId);

        return productoRepository
                .findByCategoriaIdOrderByNombreAsc(categoriaId);
    }

    @Override
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No existe un producto con ID " + id
                        )
                );
    }

    @Override
    @Transactional
    public Producto crear(
            Producto producto,
            Long categoriaId
    ) {
        validarProducto(producto);

        Categoria categoria = obtenerCategoria(categoriaId);

        String nombreNormalizado =
                InputValidator.normalizarTextoSeguro(
                        producto.getNombre(),
                        "nombre"
                );

        producto.setId(null);
        producto.setNombre(nombreNormalizado);
        producto.setCategoria(categoria);
        producto.setStock(0);

        return productoRepository.save(producto);
    }

    @Override
    @Transactional
    public Producto actualizar(
            Long id,
            Producto producto,
            Long categoriaId
    ) {
        validarProducto(producto);

        Producto productoExistente = obtenerPorId(id);
        Categoria categoria = obtenerCategoria(categoriaId);

        String nombreNormalizado =
                InputValidator.normalizarTextoSeguro(
                        producto.getNombre(),
                        "nombre"
                );

        productoExistente.setNombre(nombreNormalizado);
        productoExistente.setPrecio(producto.getPrecio());
        productoExistente.setCategoria(categoria);

        return productoRepository.save(productoExistente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Producto producto = obtenerPorId(id);

        if (
                producto.getStock() != null
                        && producto.getStock() > 0
        ) {
            throw new BusinessConflictException(
                    "No se puede eliminar el producto porque mantiene stock"
            );
        }

        if (movimientoRepository.existsByProductoId(id)) {
            throw new BusinessConflictException(
                    "No se puede eliminar el producto porque tiene "
                            + "movimientos de inventario asociados"
            );
        }

        productoRepository.delete(producto);
    }

    private Categoria obtenerCategoria(Long categoriaId) {
        if (categoriaId == null) {
            throw new IllegalArgumentException(
                    "La categoría del producto es obligatoria"
            );
        }

        return categoriaRepository.findById(categoriaId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No existe una categoría con ID "
                                        + categoriaId
                        )
                );
    }

    private void validarProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException(
                    "El producto es obligatorio"
            );
        }

        validarPrecio(producto.getPrecio());
    }

    private void validarPrecio(BigDecimal precio) {
        if (
                precio == null
                        || precio.compareTo(BigDecimal.ZERO) <= 0
        ) {
            throw new IllegalArgumentException(
                    "El precio del producto debe ser mayor que cero"
            );
        }

        if (precio.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException(
                    "El precio debe tener como máximo dos decimales"
            );
        }
    }
}
