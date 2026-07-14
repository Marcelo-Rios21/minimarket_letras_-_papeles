package com.minimarket.hateoas;

import com.minimarket.controller.DetalleVentaController;
import com.minimarket.controller.UsuarioController;
import com.minimarket.controller.VentaController;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.entity.Venta;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class VentaModelAssembler
        implements RepresentationModelAssembler<
                Venta,
                EntityModel<Venta>
        > {

    @Override
    public EntityModel<Venta> toModel(Venta venta) {
        EntityModel<Venta> modelo = EntityModel.of(venta);

        if (venta.getId() != null) {
            modelo.add(
                    linkTo(
                            methodOn(VentaController.class)
                                    .obtenerVentaPorId(venta.getId())
                    ).withSelfRel()
            );
        }

        modelo.add(
                linkTo(
                        methodOn(VentaController.class)
                                .listarVentas()
                ).withRel("ventas")
        );

        if (
                venta.getUsuario() != null
                        && venta.getUsuario().getId() != null
        ) {
            modelo.add(
                    linkTo(
                            methodOn(UsuarioController.class)
                                    .obtenerUsuarioPorId(
                                            venta.getUsuario().getId()
                                    )
                    ).withRel("usuario")
            );
        }

        modelo.add(
                linkTo(
                        methodOn(DetalleVentaController.class)
                                .listarDetalleVentas()
                ).withRel("detalles")
        );

        if (venta.getDetalles() != null) {
            for (DetalleVenta detalle : venta.getDetalles()) {
                if (detalle != null && detalle.getId() != null) {
                    modelo.add(
                            linkTo(
                                    methodOn(
                                            DetalleVentaController.class
                                    ).obtenerDetalleVentaPorId(
                                            detalle.getId()
                                    )
                            ).withRel(
                                    "detalle-" + detalle.getId()
                            )
                    );
                }
            }
        }

        return modelo;
    }
}