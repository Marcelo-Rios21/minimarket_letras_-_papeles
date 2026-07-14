package com.minimarket.controller;

import com.minimarket.dto.error.ApiErrorResponse;
import com.minimarket.entity.Inventario;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.hateoas.InventarioModelAssembler;
import com.minimarket.service.InventarioService;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.minimarket.config.OpenApiConfig.SECURITY_SCHEME_NAME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/inventario")
@Tag(
        name = "Inventario",
        description = """
                Gestión de movimientos de entrada y salida de productos.
                Las operaciones requieren el rol EMPLEADO o GERENTE.
                """
)
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class InventarioController {

    private final InventarioService inventarioService;
    private final InventarioModelAssembler inventarioModelAssembler;

    public InventarioController(
            InventarioService inventarioService,
            InventarioModelAssembler inventarioModelAssembler
    ) {
        this.inventarioService = inventarioService;
        this.inventarioModelAssembler = inventarioModelAssembler;
    }

    @GetMapping
    @Operation(
            summary = "Listar movimientos",
            description = "Obtiene todos los movimientos de inventario registrados."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movimientos obtenidos correctamente.",
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
    public CollectionModel<EntityModel<Inventario>>
            listarMovimientosDeInventario() {

        List<EntityModel<Inventario>> movimientos =
                inventarioService.findAll()
                        .stream()
                        .map(inventarioModelAssembler::toModel)
                        .toList();

        return CollectionModel.of(
                movimientos,
                linkTo(
                        methodOn(InventarioController.class)
                                .listarMovimientosDeInventario()
                ).withSelfRel()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener movimiento por ID",
            description = "Busca un movimiento de inventario por su identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movimiento encontrado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    allOf = {
                                            Inventario.class,
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
                    description = "El movimiento solicitado no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<Inventario>> obtenerMovimientoPorId(
            @Parameter(
                    description = "Identificador del movimiento.",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        Inventario movimiento = buscarMovimiento(id);

        return ResponseEntity.ok(
                inventarioModelAssembler.toModel(
                        movimiento
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Registrar movimiento",
            description = """
                    Registra una entrada o salida de inventario asociada a un
                    producto. La cantidad debe ser mayor a cero.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Datos del movimiento que será registrado.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Inventario.class),
                    examples = @ExampleObject(
                            name = "Entrada de inventario",
                            value = """
                                    {
                                      "producto": {
                                        "id": 1
                                      },
                                      "cantidad": 25,
                                      "tipoMovimiento": "Entrada",
                                      "fechaMovimiento": "2026-07-13T18:30:00.000+00:00"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movimiento registrado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    allOf = {
                                            Inventario.class,
                                            EntityModel.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El movimiento contiene datos inválidos.",
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
                    description = "El producto asociado no puede persistirse.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<Inventario>> registrarMovimiento(
            @Valid @RequestBody Inventario inventario
    ) {
        Inventario movimientoGuardado = guardarMovimiento(inventario);

        return ResponseEntity.ok(
                inventarioModelAssembler.toModel(
                        movimientoGuardado
                )
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar movimiento",
            description = "Actualiza un movimiento de inventario existente."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Datos actualizados del movimiento.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Inventario.class),
                    examples = @ExampleObject(
                            name = "Movimiento actualizado",
                            value = """
                                    {
                                      "producto": {
                                        "id": 1
                                      },
                                      "cantidad": 10,
                                      "tipoMovimiento": "Salida",
                                      "fechaMovimiento": "2026-07-13T19:00:00.000+00:00"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Movimiento actualizado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    allOf = {
                                            Inventario.class,
                                            EntityModel.class
                                    }
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "El movimiento contiene datos inválidos.",
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
                    description = "El movimiento solicitado no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El movimiento no puede persistirse por sus relaciones.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<Inventario>> actualizarMovimiento(
            @Parameter(
                    description = "Identificador del movimiento.",
                    example = "1"
            )
            @PathVariable Long id,
            @Valid @RequestBody Inventario inventario
    ) {
        buscarMovimiento(id);
        inventario.setId(id);

        Inventario movimientoActualizado =
                guardarMovimiento(inventario);

        return ResponseEntity.ok(
                inventarioModelAssembler.toModel(
                        movimientoActualizado
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar movimiento",
            description = "Elimina un movimiento de inventario mediante su ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Movimiento eliminado correctamente.",
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
                    description = "Se requiere el rol EMPLEADO o GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El movimiento solicitado no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<Void> eliminarMovimiento(
            @Parameter(
                    description = "Identificador del movimiento.",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        buscarMovimiento(id);
        inventarioService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private Inventario buscarMovimiento(Long id) {
        Inventario movimiento = inventarioService.findById(id);

        if (movimiento == null) {
            throw new ResourceNotFoundException(
                    "No existe un movimiento de inventario con ID " + id
            );
        }

        return movimiento;
    }

    private Inventario guardarMovimiento(Inventario inventario) {
        try {
            return inventarioService.registrarMovimiento(inventario);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage(),
                    exception
            );
        }
    }
}