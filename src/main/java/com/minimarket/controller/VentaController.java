package com.minimarket.controller;

import com.minimarket.dto.error.ApiErrorResponse;
import com.minimarket.entity.Venta;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.service.VentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static com.minimarket.config.OpenApiConfig.SECURITY_SCHEME_NAME;

@RestController
@RequestMapping("/api/ventas")
@Tag(
        name = "Ventas",
        description = """
                Consulta y registro de ventas. Las consultas permiten los roles
                EMPLEADO y GERENTE, pero el registro está reservado al rol EMPLEADO.
                """
)
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class VentaController {

    private final VentaService ventaService;

    public VentaController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @GetMapping
    @Operation(
            summary = "Listar ventas",
            description = """
                    Obtiene todas las ventas registradas.
                    Requiere el rol EMPLEADO o GERENTE.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Ventas obtenidas correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = Venta.class
                                    )
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
    public List<Venta> listarVentas() {
        return ventaService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener venta por ID",
            description = """
                    Busca una venta mediante su identificador.
                    Requiere el rol EMPLEADO o GERENTE.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Venta encontrada correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = Venta.class
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
                    description = "La venta solicitada no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<Venta> obtenerVentaPorId(
            @Parameter(
                    description = "Identificador de la venta.",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        Venta venta = ventaService.findById(id);

        if (venta == null) {
            throw new ResourceNotFoundException(
                    "No existe una venta con ID " + id
            );
        }

        return ResponseEntity.ok(venta);
    }

    @PostMapping
    @Operation(
            summary = "Registrar venta",
            description = """
                    Registra una venta, verifica el usuario y el stock, obtiene
                    los precios actuales, calcula el total y descuenta las
                    unidades vendidas del inventario.

                    Esta operación está reservada exclusivamente al rol EMPLEADO.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = """
                    Solo deben enviarse el ID del usuario, el ID de cada producto
                    y la cantidad. La fecha, el precio y el total son calculados
                    por el servidor.
                    """,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Venta.class),
                    examples = @ExampleObject(
                            name = "Nueva venta",
                            value = """
                                    {
                                      "usuario": {
                                        "id": 1
                                      },
                                      "detalles": [
                                        {
                                          "producto": {
                                            "id": 1
                                          },
                                          "cantidad": 2
                                        },
                                        {
                                          "producto": {
                                            "id": 3
                                          },
                                          "cantidad": 1
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Venta registrada correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = Venta.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = """
                            Datos inválidos, usuario incompleto, producto inexistente
                            o stock insuficiente.
                            """,
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
                    description = "La operación requiere exclusivamente el rol EMPLEADO.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "La venta no pudo persistirse por un conflicto de datos.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<Venta> guardarVenta(
            @Valid @RequestBody Venta venta
    ) {
        try {
            Venta ventaRegistrada = ventaService.registrarVenta(venta);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(ventaRegistrada);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage(),
                    exception
            );
        }
    }
}