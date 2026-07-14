package com.minimarket.controller;

import com.minimarket.dto.error.ApiErrorResponse;
import com.minimarket.entity.Categoria;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.hateoas.CategoriaModelAssembler;
import com.minimarket.security.util.InputValidator;
import com.minimarket.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.minimarket.config.OpenApiConfig.SECURITY_SCHEME_NAME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/categorias")
@Tag(
        name = "Categorías",
        description = """
                Operaciones REST para consultar, crear, actualizar y eliminar
                categorías utilizadas para clasificar los productos.
                """
)
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class CategoriaController {

    private final CategoriaService categoriaService;
    private final CategoriaModelAssembler categoriaModelAssembler;

    public CategoriaController(
            CategoriaService categoriaService,
            CategoriaModelAssembler categoriaModelAssembler
    ) {
        this.categoriaService = categoriaService;
        this.categoriaModelAssembler = categoriaModelAssembler;
    }

    @GetMapping
    @Operation(
            summary = "Listar categorías",
            description = "Obtiene todas las categorías registradas en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categorías obtenidas correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = CollectionModel.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token JWT válido.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee permisos suficientes.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public CollectionModel<EntityModel<Categoria>> listarCategorias() {
        List<EntityModel<Categoria>> categorias =
                categoriaService.findAll()
                        .stream()
                        .map(categoriaModelAssembler::toModel)
                        .toList();

        return CollectionModel.of(
                categorias,
                linkTo(
                        methodOn(CategoriaController.class)
                                .listarCategorias()
                ).withSelfRel()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener categoría por ID",
            description = "Busca una categoría mediante su identificador único."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría encontrada correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    allOf = {
                                            Categoria.class,
                                            EntityModel.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token JWT válido.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee permisos suficientes.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría solicitada no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<Categoria>> obtenerCategoriaPorId(
            @Parameter(
                    description = "Identificador de la categoría.",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        Categoria categoria = buscarCategoria(id);
        return ResponseEntity.ok(
                categoriaModelAssembler.toModel(categoria)
        );
    }

    @PostMapping
    @Operation(
            summary = "Crear categoría",
            description = """
                    Registra una nueva categoría. El nombre debe ser obligatorio,
                    seguro y no puede encontrarse duplicado.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Datos de la categoría que se desea crear.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Categoria.class),
                    examples = @ExampleObject(
                            name = "Nueva categoría",
                            value = """
                                    {
                                      "nombre": "Artículos escolares"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Categoría creada correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    allOf = {
                                            Categoria.class,
                                            EntityModel.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos enviados no cumplen las validaciones.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token JWT válido.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee permisos para crear categorías.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Ya existe una categoría con el mismo nombre.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<Categoria>> guardarCategoria(
            @Valid @RequestBody Categoria categoria
    ) {
        validarNombre(categoria);

        Categoria categoriaGuardada = categoriaService.save(categoria);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        categoriaModelAssembler.toModel(
                                categoriaGuardada
                        )
                );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar categoría",
            description = """
                    Actualiza el nombre de una categoría existente mediante su ID.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Datos actualizados de la categoría.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Categoria.class),
                    examples = @ExampleObject(
                            name = "Categoría actualizada",
                            value = """
                                    {
                                      "nombre": "Papelería y oficina"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Categoría actualizada correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    allOf = {
                                            Categoria.class,
                                            EntityModel.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Los datos enviados no cumplen las validaciones.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token JWT válido.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee permisos para actualizar categorías.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría solicitada no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El nombre ya pertenece a otra categoría.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<Categoria>> actualizarCategoria(
            @Parameter(
                    description = "Identificador de la categoría.",
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody Categoria categoria
    ) {
        buscarCategoria(id);
        validarNombre(categoria);

        categoria.setId(id);

        Categoria categoriaActualizada =
                categoriaService.save(categoria);

        return ResponseEntity.ok(
                categoriaModelAssembler.toModel(
                        categoriaActualizada
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar categoría",
            description = "Elimina una categoría existente mediante su ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Categoría eliminada correctamente.",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No se proporcionó un token JWT válido.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "El usuario no posee permisos para eliminar categorías.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "La categoría solicitada no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<Void> eliminarCategoria(
            @Parameter(
                    description = "Identificador de la categoría.",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        buscarCategoria(id);
        categoriaService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private Categoria buscarCategoria(Long id) {
        Categoria categoria = categoriaService.findById(id);

        if (categoria == null) {
            throw new ResourceNotFoundException(
                    "No existe una categoría con ID " + id
            );
        }

        return categoria;
    }

    private void validarNombre(Categoria categoria) {
        InputValidator.validarTextoSeguro(
                categoria.getNombre(),
                "nombre"
        );
    }
}