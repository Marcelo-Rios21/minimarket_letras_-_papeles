package com.minimarket.productoinventario.assembler;

import com.minimarket.productoinventario.controller.CategoriaController;
import com.minimarket.productoinventario.controller.InventarioController;
import com.minimarket.productoinventario.controller.ProductoController;
import com.minimarket.productoinventario.dto.ProductoResponse;
import com.minimarket.productoinventario.entity.Producto;
import com.minimarket.productoinventario.mapper.ProductoMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ProductoModelAssembler
        implements RepresentationModelAssembler<
                Producto,
                EntityModel<ProductoResponse>
        > {

    @Override
    public EntityModel<ProductoResponse> toModel(
            Producto producto
    ) {
        ProductoResponse response =
                ProductoMapper.toResponse(producto);

        return EntityModel.of(
                response,
                linkTo(
                        methodOn(ProductoController.class)
                                .obtenerPorId(producto.getId())
                ).withSelfRel(),
                linkTo(
                        methodOn(ProductoController.class)
                                .listar()
                ).withRel("productos"),
                linkTo(
                        methodOn(CategoriaController.class)
                                .obtenerPorId(
                                        producto
                                                .getCategoria()
                                                .getId()
                                )
                ).withRel("categoria"),
                linkTo(
                        methodOn(InventarioController.class)
                                .listarPorProducto(
                                        producto.getId()
                                )
                ).withRel("movimientos")
        );
    }
}
