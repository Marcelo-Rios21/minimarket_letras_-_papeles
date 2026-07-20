package com.minimarket.productoinventario.mapper;

import com.minimarket.productoinventario.dto.ProductoRequest;
import com.minimarket.productoinventario.dto.ProductoResponse;
import com.minimarket.productoinventario.entity.Categoria;
import com.minimarket.productoinventario.entity.Producto;

public final class ProductoMapper {

    private ProductoMapper() {
    }

    public static Producto toEntity(ProductoRequest request) {
        Producto producto = new Producto();
        producto.setNombre(request.nombre());
        producto.setPrecio(request.precio());
        return producto;
    }

    public static ProductoResponse toResponse(Producto producto) {
        Categoria categoria = producto.getCategoria();

        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                categoria.getId(),
                categoria.getNombre()
        );
    }
}
