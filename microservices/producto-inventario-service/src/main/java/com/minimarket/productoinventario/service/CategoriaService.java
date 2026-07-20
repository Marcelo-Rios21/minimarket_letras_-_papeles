package com.minimarket.productoinventario.service;

import com.minimarket.productoinventario.entity.Categoria;

import java.util.List;

public interface CategoriaService {

    List<Categoria> listar();

    Categoria obtenerPorId(Long id);

    Categoria crear(Categoria categoria);

    Categoria actualizar(Long id, Categoria categoria);

    void eliminar(Long id);
}
