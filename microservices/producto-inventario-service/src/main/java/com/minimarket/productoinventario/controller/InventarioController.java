package com.minimarket.productoinventario.controller;

import com.minimarket.productoinventario.assembler.MovimientoInventarioModelAssembler;
import com.minimarket.productoinventario.dto.MovimientoInventarioRequest;
import com.minimarket.productoinventario.dto.MovimientoInventarioResponse;
import com.minimarket.productoinventario.entity.MovimientoInventario;
import com.minimarket.productoinventario.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/productos/{productoId}/movimientos")
@Validated
@Tag(
        name = "Inventario",
        description = "Entradas, salidas e historial de stock"
)
public class InventarioController {

    private final InventarioService inventarioService;
    private final MovimientoInventarioModelAssembler assembler;

    public InventarioController(
            InventarioService inventarioService,
            MovimientoInventarioModelAssembler assembler
    ) {
        this.inventarioService = inventarioService;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Consultar el historial de un producto")
    public CollectionModel<
            EntityModel<MovimientoInventarioResponse>
    > listarPorProducto(
            @PathVariable
            @Positive(message = "El ID del producto debe ser positivo")
            Long productoId
    ) {
        CollectionModel<
                EntityModel<MovimientoInventarioResponse>
        > model = assembler.toCollectionModel(
                inventarioService.listarPorProducto(productoId)
        );

        model.add(
                linkTo(
                        methodOn(InventarioController.class)
                                .listarPorProducto(productoId)
                ).withSelfRel(),
                linkTo(
                        methodOn(ProductoController.class)
                                .obtenerPorId(productoId)
                ).withRel("producto")
        );

        return model;
    }

    @PostMapping
    @Operation(summary = "Registrar una entrada o salida de stock")
    public ResponseEntity<
            EntityModel<MovimientoInventarioResponse>
    > registrarMovimiento(
            @PathVariable
            @Positive(message = "El ID del producto debe ser positivo")
            Long productoId,
            @Valid
            @RequestBody
            MovimientoInventarioRequest request
    ) {
        MovimientoInventario movimiento =
                inventarioService.registrarMovimiento(
                        productoId,
                        request.tipoMovimiento(),
                        request.cantidad()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assembler.toModel(movimiento));
    }
}
