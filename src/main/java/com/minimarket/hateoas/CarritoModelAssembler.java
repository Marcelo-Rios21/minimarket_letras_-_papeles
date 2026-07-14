package com.minimarket.hateoas;

import com.minimarket.controller.CarritoController;
import com.minimarket.controller.ProductoController;
import com.minimarket.controller.UsuarioController;
import com.minimarket.entity.Carrito;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CarritoModelAssembler
        implements RepresentationModelAssembler<
                Carrito,
                EntityModel<Carrito>
        > {

    @Override
    public EntityModel<Carrito> toModel(Carrito carrito) {
        EntityModel<Carrito> modelo = EntityModel.of(carrito);

        if (carrito.getId() != null) {
            modelo.add(
                    linkTo(
                            methodOn(CarritoController.class)
                                    .obtenerCarritoPorId(carrito.getId())
                    ).withSelfRel()
            );
        }

        modelo.add(
                linkTo(
                        methodOn(CarritoController.class)
                                .listarCarrito()
                ).withRel("carrito")
        );

        if (
                carrito.getUsuario() != null
                        && carrito.getUsuario().getId() != null
        ) {
            modelo.add(
                    linkTo(
                            methodOn(UsuarioController.class)
                                    .obtenerUsuarioPorId(
                                            carrito.getUsuario().getId()
                                    )
                    ).withRel("usuario")
            );
        }

        if (
                carrito.getProducto() != null
                        && carrito.getProducto().getId() != null
        ) {
            modelo.add(
                    linkTo(
                            methodOn(ProductoController.class)
                                    .obtenerProductoPorId(
                                            carrito.getProducto().getId()
                                    )
                    ).withRel("producto")
            );
        }

        return modelo;
    }
}