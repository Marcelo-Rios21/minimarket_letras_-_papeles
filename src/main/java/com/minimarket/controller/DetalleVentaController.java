package com.minimarket.controller;

import com.minimarket.dto.error.ApiErrorResponse;
import com.minimarket.entity.DetalleVenta;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.hateoas.DetalleVentaModelAssembler;
import com.minimarket.service.DetalleVentaService;
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
@RequestMapping("/api/detalle-ventas")
@Tag(
        name = "Detalle de ventas",
        description = """
                Administración individual de los productos asociados a una venta.
                Normalmente los detalles se generan automáticamente al registrar
                una venta mediante POST /api/ventas.
                """
)
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class DetalleVentaController {

    private final DetalleVentaService detalleVentaService;
    private final DetalleVentaModelAssembler detalleVentaModelAssembler;

    public DetalleVentaController(
            DetalleVentaService detalleVentaService,
            DetalleVentaModelAssembler detalleVentaModelAssembler
    ) {
        this.detalleVentaService = detalleVentaService;
        this.detalleVentaModelAssembler =
                detalleVentaModelAssembler;
    }

    @GetMapping
    @Operation(
            summary = "Listar detalles de ventas",
            description = """
                    Obtiene todos los detalles registrados.
                    Requiere el rol EMPLEADO o GERENTE.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalles obtenidos correctamente.",
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
                    description = "Se requiere el rol EMPLEADO o GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public CollectionModel<EntityModel<DetalleVenta>>
            listarDetalleVentas() {

        List<EntityModel<DetalleVenta>> detalles =
                detalleVentaService.findAll()
                        .stream()
                        .map(detalleVentaModelAssembler::toModel)
                        .toList();

        return CollectionModel.of(
                detalles,
                linkTo(
                        methodOn(DetalleVentaController.class)
                                .listarDetalleVentas()
                ).withSelfRel()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener detalle por ID",
            description = "Busca un detalle de venta mediante su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle encontrado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    allOf = {
                                            DetalleVenta.class,
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
                    description = "Se requiere el rol EMPLEADO o GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El detalle solicitado no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<DetalleVenta>>
            obtenerDetalleVentaPorId(
            @Parameter(
                    description = "Identificador del detalle.",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        DetalleVenta detalle = buscarDetalle(id);

        return ResponseEntity.ok(
                detalleVentaModelAssembler.toModel(
                        detalle
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Crear detalle de venta",
            description = """
                    Registra directamente un detalle asociado a una venta.
                    En este endpoint administrativo deben enviarse la venta,
                    el producto, la cantidad y el precio.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Datos del detalle que será registrado.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DetalleVenta.class),
                    examples = @ExampleObject(
                            name = "Nuevo detalle",
                            value = """
                                    {
                                      "venta": {
                                        "id": 1
                                      },
                                      "producto": {
                                        "id": 2
                                      },
                                      "cantidad": 3,
                                      "precio": 1490.0
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle registrado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    allOf = {
                                            DetalleVenta.class,
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
                    description = "Se requiere el rol EMPLEADO o GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La venta o el producto asociado no puede persistirse.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<DetalleVenta>>
            guardarDetalleVenta(
            @Valid @RequestBody DetalleVenta detalleVenta
    ) {
        DetalleVenta detalleGuardado =
                detalleVentaService.save(detalleVenta);

        return ResponseEntity.ok(
                detalleVentaModelAssembler.toModel(
                        detalleGuardado
                )
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar detalle de venta",
            description = """
                    Actualiza los datos de un detalle existente.
                    Requiere el rol EMPLEADO o GERENTE.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Datos actualizados del detalle.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = DetalleVenta.class),
                    examples = @ExampleObject(
                            name = "Detalle actualizado",
                            value = """
                                    {
                                      "venta": {
                                        "id": 1
                                      },
                                      "producto": {
                                        "id": 2
                                      },
                                      "cantidad": 4,
                                      "precio": 1490.0
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Detalle actualizado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    allOf = {
                                            DetalleVenta.class,
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
                    description = "Se requiere el rol EMPLEADO o GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El detalle solicitado no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La venta o el producto asociado no puede persistirse.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<DetalleVenta>>
            actualizarDetalleVenta(
            @Parameter(
                    description = "Identificador del detalle.",
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody DetalleVenta detalleVenta
    ) {
        buscarDetalle(id);
        detalleVenta.setId(id);

        DetalleVenta detalleActualizado =
                detalleVentaService.save(detalleVenta);

        return ResponseEntity.ok(
                detalleVentaModelAssembler.toModel(
                        detalleActualizado
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar detalle de venta",
            description = """
                    Elimina un detalle mediante su ID.
                    Esta operación requiere el rol GERENTE.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Detalle eliminado correctamente.",
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
                    description = "La operación requiere el rol GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El detalle solicitado no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El detalle no puede eliminarse por sus relaciones.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<Void> eliminarDetalleVenta(
            @Parameter(
                    description = "Identificador del detalle.",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        buscarDetalle(id);
        detalleVentaService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private DetalleVenta buscarDetalle(Long id) {
        DetalleVenta detalle = detalleVentaService.findById(id);

        if (detalle == null) {
            throw new ResourceNotFoundException(
                    "No existe un detalle de venta con ID " + id
            );
        }

        return detalle;
    }
}