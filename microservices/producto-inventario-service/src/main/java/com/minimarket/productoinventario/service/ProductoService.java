package com.minimarket.productoinventario.service;

import com.minimarket.productoinventario.entity.Producto;

import java.util.List;

public interface ProductoService {

    List<Producto> listar();

    List<Producto> listarPorCategoria(Long categoriaId);

    Producto obtenerPorId(Long id);

    Producto crear(
            Producto producto,
            Long categoriaId
    );

    Producto actualizar(
            Long id,
            Producto producto,
            Long categoriaId
    );

    void eliminar(Long id);
}
