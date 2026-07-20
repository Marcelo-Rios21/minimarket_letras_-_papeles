package com.minimarket.productoinventario.controller;

import com.minimarket.productoinventario.assembler.CategoriaModelAssembler;
import com.minimarket.productoinventario.dto.CategoriaRequest;
import com.minimarket.productoinventario.dto.CategoriaResponse;
import com.minimarket.productoinventario.entity.Categoria;
import com.minimarket.productoinventario.mapper.CategoriaMapper;
import com.minimarket.productoinventario.service.CategoriaService;
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
@RequestMapping("/api/categorias")
@Validated
@Tag(
        name = "Categorías",
        description = "Administración de categorías de productos"
)
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaModelAssembler assembler;

    public CategoriaController(
            CategoriaService categoriaService,
            CategoriaModelAssembler assembler
    ) {
        this.categoriaService = categoriaService;
        this.assembler = assembler;
    }

    @GetMapping
    @Operation(summary = "Listar todas las categorías")
    public CollectionModel<EntityModel<CategoriaResponse>> listar() {
        CollectionModel<EntityModel<CategoriaResponse>> model =
                assembler.toCollectionModel(
                        categoriaService.listar()
                );

        model.add(
                linkTo(
                        methodOn(CategoriaController.class).listar()
                ).withSelfRel()
        );

        return model;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una categoría por ID")
    public EntityModel<CategoriaResponse> obtenerPorId(
            @PathVariable
            @Positive(message = "El ID debe ser positivo")
            Long id
    ) {
        return assembler.toModel(
                categoriaService.obtenerPorId(id)
        );
    }

    @PostMapping
    @Operation(summary = "Crear una nueva categoría")
    public ResponseEntity<EntityModel<CategoriaResponse>> crear(
            @Valid
            @RequestBody
            CategoriaRequest request
    ) {
        Categoria categoria = categoriaService.crear(
                CategoriaMapper.toEntity(request)
        );

        EntityModel<CategoriaResponse> model =
                assembler.toModel(categoria);

        URI location = URI.create(
                model.getRequiredLink("self").getHref()
        );

        return ResponseEntity.created(location).body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una categoría")
    public EntityModel<CategoriaResponse> actualizar(
            @PathVariable
            @Positive(message = "El ID debe ser positivo")
            Long id,
            @Valid
            @RequestBody
            CategoriaRequest request
    ) {
        Categoria categoria = categoriaService.actualizar(
                id,
                CategoriaMapper.toEntity(request)
        );

        return assembler.toModel(categoria);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una categoría sin productos")
    public ResponseEntity<Void> eliminar(
            @PathVariable
            @Positive(message = "El ID debe ser positivo")
            Long id
    ) {
        categoriaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
