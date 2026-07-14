package com.minimarket.hateoas;

import com.minimarket.controller.CategoriaController;
import com.minimarket.controller.ProductoController;
import com.minimarket.entity.Categoria;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoriaModelAssembler
        implements RepresentationModelAssembler<
                Categoria,
                EntityModel<Categoria>
        > {

    @Override
    public EntityModel<Categoria> toModel(Categoria categoria) {
        EntityModel<Categoria> modelo = EntityModel.of(categoria);

        if (categoria.getId() != null) {
            modelo.add(
                    linkTo(
                            methodOn(CategoriaController.class)
                                    .obtenerCategoriaPorId(categoria.getId())
                    ).withSelfRel()
            );
        }

        modelo.add(
                linkTo(
                        methodOn(CategoriaController.class)
                                .listarCategorias()
                ).withRel("categorias")
        );

        modelo.add(
                linkTo(
                        methodOn(ProductoController.class)
                                .listarProductos()
                ).withRel("productos")
        );

        return modelo;
    }
}