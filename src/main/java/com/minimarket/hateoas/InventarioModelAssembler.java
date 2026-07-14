package com.minimarket.hateoas;

import com.minimarket.controller.InventarioController;
import com.minimarket.controller.ProductoController;
import com.minimarket.entity.Inventario;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class InventarioModelAssembler
        implements RepresentationModelAssembler<
                Inventario,
                EntityModel<Inventario>
        > {

    @Override
    public EntityModel<Inventario> toModel(Inventario inventario) {
        EntityModel<Inventario> modelo = EntityModel.of(inventario);

        if (inventario.getId() != null) {
            modelo.add(
                    linkTo(
                            methodOn(InventarioController.class)
                                    .obtenerMovimientoPorId(
                                            inventario.getId()
                                    )
                    ).withSelfRel()
            );
        }

        modelo.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .listarMovimientosDeInventario()
                ).withRel("inventario")
        );

        if (
                inventario.getProducto() != null
                        && inventario.getProducto().getId() != null
        ) {
            modelo.add(
                    linkTo(
                            methodOn(ProductoController.class)
                                    .obtenerProductoPorId(
                                            inventario
                                                    .getProducto()
                                                    .getId()
                                    )
                    ).withRel("producto")
            );
        }

        return modelo;
    }
}