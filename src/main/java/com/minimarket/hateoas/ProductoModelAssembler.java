package com.minimarket.hateoas;

import com.minimarket.controller.CategoriaController;
import com.minimarket.controller.ProductoController;
import com.minimarket.entity.Producto;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductoModelAssembler
        implements RepresentationModelAssembler<
                Producto,
                EntityModel<Producto>
        > {

    @Override
    public EntityModel<Producto> toModel(Producto producto) {
        EntityModel<Producto> modelo = EntityModel.of(producto);

        if (producto.getId() != null) {
            modelo.add(
                    linkTo(
                            methodOn(ProductoController.class)
                                    .obtenerProductoPorId(producto.getId())
                    ).withSelfRel()
            );
        }

        modelo.add(
                linkTo(
                        methodOn(ProductoController.class)
                                .listarProductos()
                ).withRel("productos")
        );

        if (
                producto.getCategoria() != null
                        && producto.getCategoria().getId() != null
        ) {
            modelo.add(
                    linkTo(
                            methodOn(CategoriaController.class)
                                    .obtenerCategoriaPorId(
                                            producto.getCategoria().getId()
                                    )
                    ).withRel("categoria")
            );
        }

        return modelo;
    }
}