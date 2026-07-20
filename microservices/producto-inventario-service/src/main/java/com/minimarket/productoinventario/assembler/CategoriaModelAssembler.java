package com.minimarket.productoinventario.assembler;

import com.minimarket.productoinventario.controller.CategoriaController;
import com.minimarket.productoinventario.controller.ProductoController;
import com.minimarket.productoinventario.dto.CategoriaResponse;
import com.minimarket.productoinventario.entity.Categoria;
import com.minimarket.productoinventario.mapper.CategoriaMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CategoriaModelAssembler
        implements RepresentationModelAssembler<
                Categoria,
                EntityModel<CategoriaResponse>
        > {

    @Override
    public EntityModel<CategoriaResponse> toModel(
            Categoria categoria
    ) {
        CategoriaResponse response =
                CategoriaMapper.toResponse(categoria);

        return EntityModel.of(
                response,
                linkTo(
                        methodOn(CategoriaController.class)
                                .obtenerPorId(categoria.getId())
                ).withSelfRel(),
                linkTo(
                        methodOn(CategoriaController.class)
                                .listar()
                ).withRel("categorias"),
                linkTo(
                        methodOn(ProductoController.class)
                                .listarPorCategoria(
                                        categoria.getId()
                                )
                ).withRel("productos")
        );
    }
}
