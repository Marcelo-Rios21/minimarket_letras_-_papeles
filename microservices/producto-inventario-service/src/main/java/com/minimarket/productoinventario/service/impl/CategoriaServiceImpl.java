package com.minimarket.productoinventario.service.impl;

import com.minimarket.productoinventario.entity.Categoria;
import com.minimarket.productoinventario.exception.BusinessConflictException;
import com.minimarket.productoinventario.exception.ResourceNotFoundException;
import com.minimarket.productoinventario.repository.CategoriaRepository;
import com.minimarket.productoinventario.repository.ProductoRepository;
import com.minimarket.productoinventario.service.CategoriaService;
import com.minimarket.productoinventario.validation.InputValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    public CategoriaServiceImpl(
            CategoriaRepository categoriaRepository,
            ProductoRepository productoRepository
    ) {
        this.categoriaRepository = categoriaRepository;
        this.productoRepository = productoRepository;
    }

    @Override
    public List<Categoria> listar() {
        return categoriaRepository.findAll();
    }

    @Override
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No existe una categoría con ID " + id
                        )
                );
    }

    @Override
    @Transactional
    public Categoria crear(Categoria categoria) {
        validarCategoria(categoria);

        String nombreNormalizado =
                InputValidator.normalizarTextoSeguro(
                        categoria.getNombre(),
                        "nombre"
                );

        if (categoriaRepository.existsByNombreIgnoreCase(
                nombreNormalizado
        )) {
            throw new BusinessConflictException(
                    "Ya existe una categoría con el nombre "
                            + nombreNormalizado
            );
        }

        categoria.setId(null);
        categoria.setNombre(nombreNormalizado);

        return categoriaRepository.save(categoria);
    }

    @Override
    @Transactional
    public Categoria actualizar(Long id, Categoria categoria) {
        validarCategoria(categoria);

        Categoria categoriaExistente = obtenerPorId(id);

        String nombreNormalizado =
                InputValidator.normalizarTextoSeguro(
                        categoria.getNombre(),
                        "nombre"
                );

        if (
                categoriaRepository
                        .existsByNombreIgnoreCaseAndIdNot(
                                nombreNormalizado,
                                id
                        )
        ) {
            throw new BusinessConflictException(
                    "Ya existe otra categoría con el nombre "
                            + nombreNormalizado
            );
        }

        categoriaExistente.setNombre(nombreNormalizado);

        return categoriaRepository.save(categoriaExistente);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = obtenerPorId(id);

        if (productoRepository.existsByCategoriaId(id)) {
            throw new BusinessConflictException(
                    "No se puede eliminar la categoría porque tiene "
                            + "productos asociados"
            );
        }

        categoriaRepository.delete(categoria);
    }

    private void validarCategoria(Categoria categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException(
                    "La categoría es obligatoria"
            );
        }
    }
}
