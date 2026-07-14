package com.minimarket.controller;

import com.minimarket.entity.Carrito;
import com.minimarket.hateoas.CarritoModelAssembler;
import com.minimarket.service.CarritoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.minimarket.config.OpenApiConfig.SECURITY_SCHEME_NAME;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/carrito")
@Tag(name = "Carrito", description = "Operaciones REST para consultar, agregar, actualizar y eliminar productos del carrito.")
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class CarritoController {

    private final CarritoService carritoService;
    private final CarritoModelAssembler carritoModelAssembler;

    public CarritoController(
            CarritoService carritoService,
            CarritoModelAssembler carritoModelAssembler
    ) {
        this.carritoService = carritoService;
        this.carritoModelAssembler = carritoModelAssembler;
    }

    @GetMapping
    @Operation(
            summary = "Listar carrito",
            description = "Obtiene todos los registros asociados al carrito."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Listado del carrito obtenido correctamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = CollectionModel.class
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder al recurso.", content = @Content)
    })
    public CollectionModel<EntityModel<Carrito>> listarCarrito() {
        List<EntityModel<Carrito>> items =
                carritoService.findAll()
                        .stream()
                        .map(carritoModelAssembler::toModel)
                        .toList();

        return CollectionModel.of(
                items,
                linkTo(
                        methodOn(CarritoController.class)
                                .listarCarrito()
                ).withSelfRel()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener item del carrito por ID",
            description = "Busca un registro especifico del carrito utilizando su identificador unico."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Registro del carrito encontrado correctamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    allOf = {
                                            Carrito.class,
                                            EntityModel.class
                                    }
                            )
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder al recurso.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro del carrito no encontrado.", content = @Content)
    })
    public ResponseEntity<EntityModel<Carrito>> obtenerCarritoPorId(
            @Parameter(description = "ID del registro del carrito que se desea consultar.", example = "1")
            @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);

        if (carrito == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(
                carritoModelAssembler.toModel(carrito)
        );
    }

    @PostMapping
    @Operation(
            summary = "Agregar producto al carrito",
            description = "Registra un nuevo item en el carrito.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos del item que se desea agregar al carrito.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Carrito.class),
                            examples = @ExampleObject(
                                    name = "Item de carrito ejemplo",
                                    value = """
                                            {
                                              "cantidad": 2
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item agregado al carrito correctamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    allOf = {
                                            Carrito.class,
                                            EntityModel.class
                                    }
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida.", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para agregar items al carrito.", content = @Content)
    })
    public EntityModel<Carrito> agregarProductoAlCarrito(@RequestBody Carrito carrito) {
        Carrito carritoGuardado = carritoService.save(carrito);

        return carritoModelAssembler.toModel(
                carritoGuardado
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar item del carrito",
            description = "Actualiza un registro existente del carrito a partir de su ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos actualizados del item del carrito.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Carrito.class),
                            examples = @ExampleObject(
                                    name = "Actualizacion de carrito",
                                    value = """
                                            {
                                              "cantidad": 3
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item del carrito actualizado correctamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    allOf = {
                                            Carrito.class,
                                            EntityModel.class
                                    }
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida.", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para actualizar el carrito.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro del carrito no encontrado.", content = @Content)
    })
    public ResponseEntity<EntityModel<Carrito>> actualizarCarrito(
            @Parameter(description = "ID del registro del carrito que se desea actualizar.", example = "1")
            @PathVariable Long id,
            @RequestBody Carrito carrito) {
        Carrito existente = carritoService.findById(id);

        if (existente == null) {
            return ResponseEntity.notFound().build();
        }

        carrito.setId(id);
        Carrito carritoActualizado = carritoService.save(carrito);

        return ResponseEntity.ok(
                carritoModelAssembler.toModel(
                        carritoActualizado
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar item del carrito",
            description = "Elimina un registro existente del carrito a partir de su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Item eliminado del carrito correctamente.", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para eliminar items del carrito.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro del carrito no encontrado.", content = @Content)
    })
    public ResponseEntity<Void> eliminarProductoDelCarrito(
            @Parameter(description = "ID del registro del carrito que se desea eliminar.", example = "1")
            @PathVariable Long id) {
        Carrito carrito = carritoService.findById(id);
        if (carrito != null) {
            carritoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}