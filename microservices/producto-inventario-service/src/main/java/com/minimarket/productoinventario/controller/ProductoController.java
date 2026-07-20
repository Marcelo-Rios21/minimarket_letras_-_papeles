package com.minimarket.productoinventario.controller;

import com.minimarket.productoinventario.assembler.ProductoModelAssembler;
import com.minimarket.productoinventario.dto.ProductoRequest;
import com.minimarket.productoinventario.dto.ProductoResponse;
import com.minimarket.productoinventario.entity.Producto;
import com.minimarket.productoinventario.mapper.ProductoMapper;
import com.minimarket.productoinventario.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/productos")
@Validated
@Tag(
        name = "Productos",
        description = "Administración del catálogo de productos"
)
public class ProductoController {

    private final ProductoService productoService;
    private final ProductoModelAssembler assembler;

    public ProductoController(
            ProductoService productoService,
            ProductoModelAssembler assembler
    ) {
        this.productoService = productoService;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos")
    public CollectionModel<EntityModel<ProductoResponse>> listar() {
        CollectionModel<EntityModel<ProductoResponse>> model =
                assembler.toCollectionModel(
                        productoService.listar()
                );

        model.add(
                linkTo(
                        methodOn(ProductoController.class).listar()
                ).withSelfRel()
        );

        return model;
    }

    @GetMapping("/categoria/{categoriaId}")
    @Operation(summary = "Listar productos de una categoría")
    public CollectionModel<EntityModel<ProductoResponse>>
            listarPorCategoria(
                    @PathVariable
                    @Positive(
                            message =
                                    "El ID de la categoría debe ser positivo"
                    )
                    Long categoriaId
            ) {
        CollectionModel<EntityModel<ProductoResponse>> model =
                assembler.toCollectionModel(
                        productoService.listarPorCategoria(
                                categoriaId
                        )
                );

        model.add(
                linkTo(
                        methodOn(ProductoController.class)
                                .listarPorCategoria(categoriaId)
                ).withSelfRel(),
                linkTo(
                        methodOn(CategoriaController.class)
                                .obtenerPorId(categoriaId)
                ).withRel("categoria")
        );

        return model;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un producto por ID")
    public EntityModel<ProductoResponse> obtenerPorId(
            @PathVariable
            @Positive(message = "El ID debe ser positivo")
            Long id
    ) {
        return assembler.toModel(
                productoService.obtenerPorId(id)
        );
    }

    @PostMapping
    @Operation(summary = "Crear un producto con stock inicial cero")
    public ResponseEntity<EntityModel<ProductoResponse>> crear(
            @Valid
            @RequestBody
            ProductoRequest request
    ) {
        Producto producto = productoService.crear(
                ProductoMapper.toEntity(request),
                request.categoriaId()
        );

        EntityModel<ProductoResponse> model =
                assembler.toModel(producto);

        URI location = URI.create(
                model.getRequiredLink("self").getHref()
        );

        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar nombre, precio y categoría de un producto"
    )
    public EntityModel<ProductoResponse> actualizar(
            @PathVariable
            @Positive(message = "El ID debe ser positivo")
            Long id,
            @Valid
            @RequestBody
            ProductoRequest request
    ) {
        Producto producto = productoService.actualizar(
                id,
                ProductoMapper.toEntity(request),
                request.categoriaId()
        );

        return assembler.toModel(producto);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un producto sin stock ni movimientos"
    )
    public ResponseEntity<Void> eliminar(
            @PathVariable
            @Positive(message = "El ID debe ser positivo")
            Long id
    ) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
