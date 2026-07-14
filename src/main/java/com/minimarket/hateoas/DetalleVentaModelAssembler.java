package com.minimarket.hateoas;

import com.minimarket.controller.DetalleVentaController;
import com.minimarket.controller.ProductoController;
import com.minimarket.controller.VentaController;
import com.minimarket.entity.DetalleVenta;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class DetalleVentaModelAssembler
        implements RepresentationModelAssembler<
                DetalleVenta,
                EntityModel<DetalleVenta>
        > {

    @Override
    public EntityModel<DetalleVenta> toModel(
            DetalleVenta detalle
    ) {
        EntityModel<DetalleVenta> modelo =
                EntityModel.of(detalle);

        if (detalle.getId() != null) {
            modelo.add(
                    linkTo(
                            methodOn(DetalleVentaController.class)
                                    .obtenerDetalleVentaPorId(
                                            detalle.getId()
                                    )
                    ).withSelfRel()
            );
        }

        modelo.add(
                linkTo(
                        methodOn(DetalleVentaController.class)
                                .listarDetalleVentas()
                ).withRel("detalles")
        );

        if (
                detalle.getProducto() != null
                        && detalle.getProducto().getId() != null
        ) {
            modelo.add(
                    linkTo(
                            methodOn(ProductoController.class)
                                    .obtenerProductoPorId(
                                            detalle
                                                    .getProducto()
                                                    .getId()
                                    )
                    ).withRel("producto")
            );
        }

        if (
                detalle.getVenta() != null
                        && detalle.getVenta().getId() != null
        ) {
            modelo.add(
                    linkTo(
                            methodOn(VentaController.class)
                                    .obtenerVentaPorId(
                                            detalle.getVenta().getId()
                                    )
                    ).withRel("venta")
            );
        }

        return modelo;
    }
}