package com.minimarket.productoinventario.assembler;

import com.minimarket.productoinventario.controller.InventarioController;
import com.minimarket.productoinventario.controller.ProductoController;
import com.minimarket.productoinventario.dto.MovimientoInventarioResponse;
import com.minimarket.productoinventario.entity.MovimientoInventario;
import com.minimarket.productoinventario.mapper.MovimientoInventarioMapper;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class MovimientoInventarioModelAssembler
        implements RepresentationModelAssembler<
                MovimientoInventario,
                EntityModel<MovimientoInventarioResponse>
        > {

    @Override
    public EntityModel<MovimientoInventarioResponse> toModel(
            MovimientoInventario movimiento
    ) {
        Long productoId =
                movimiento.getProducto().getId();

        return EntityModel.of(
                MovimientoInventarioMapper.toResponse(movimiento),
                linkTo(
                        methodOn(ProductoController.class)
                                .obtenerPorId(productoId)
                ).withRel("producto"),
                linkTo(
                        methodOn(InventarioController.class)
                                .listarPorProducto(productoId)
                ).withRel("historial")
        );
    }
}
