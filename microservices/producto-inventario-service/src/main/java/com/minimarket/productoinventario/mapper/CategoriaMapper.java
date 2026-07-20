package com.minimarket.productoinventario.mapper;

import com.minimarket.productoinventario.dto.CategoriaRequest;
import com.minimarket.productoinventario.dto.CategoriaResponse;
import com.minimarket.productoinventario.entity.Categoria;

public final class CategoriaMapper {

    private CategoriaMapper() {
    }

    public static Categoria toEntity(CategoriaRequest request) {
        Categoria categoria = new Categoria();
        categoria.setNombre(request.nombre());
        return categoria;
    }

    public static CategoriaResponse toResponse(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre()
        );
    }
}
