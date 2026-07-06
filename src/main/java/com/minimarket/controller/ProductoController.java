package com.minimarket.controller;

import com.minimarket.entity.Producto;
import com.minimarket.security.util.InputValidator;
import com.minimarket.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones REST para consultar, crear, actualizar y eliminar productos del minimarket.")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @GetMapping
    @Operation(
            summary = "Listar productos",
            description = "Obtiene la lista completa de productos registrados en el sistema."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de productos obtenida correctamente.",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Producto.class))
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder al recurso.", content = @Content)
    })
    public List<Producto> listarProductos() {
        return productoService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener producto por ID",
            description = "Busca un producto especifico utilizando su identificador unico."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto encontrado correctamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class)
                    )
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para acceder al recurso.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado.", content = @Content)
    })
    public ResponseEntity<Producto> obtenerProductoPorId(
            @Parameter(description = "ID del producto que se desea consultar.", example = "1")
            @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        return (producto != null) ? ResponseEntity.ok(producto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    @Operation(
            summary = "Crear producto",
            description = "Registra un nuevo producto en el sistema. El nombre del producto es validado antes de guardar.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos del producto que se desea crear.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class),
                            examples = @ExampleObject(
                                    name = "Producto ejemplo",
                                    value = """
                                            {
                                              "nombre": "Cuaderno universitario",
                                              "precio": 2490,
                                              "stock": 50
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto creado correctamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida o datos no seguros.", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para crear productos.", content = @Content)
    })
    public Producto guardarProducto(@RequestBody Producto producto) {
        InputValidator.validarTextoSeguro(producto.getNombre(), "nombre");
        return productoService.save(producto);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar producto",
            description = "Actualiza los datos de un producto existente a partir de su ID.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Datos actualizados del producto.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class),
                            examples = @ExampleObject(
                                    name = "Actualizacion de producto",
                                    value = """
                                            {
                                              "nombre": "Lapiz pasta azul",
                                              "precio": 690,
                                              "stock": 120
                                            }
                                            """
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Producto actualizado correctamente.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Producto.class)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "Solicitud invalida o datos no seguros.", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para actualizar productos.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado.", content = @Content)
    })
    public ResponseEntity<Producto> actualizarProducto(
            @Parameter(description = "ID del producto que se desea actualizar.", example = "1")
            @PathVariable Long id,
            @RequestBody Producto producto) {
        Producto productoExistente = productoService.findById(id);
        if (productoExistente != null) {
            producto.setId(id);
            InputValidator.validarTextoSeguro(producto.getNombre(), "nombre");
            return ResponseEntity.ok(productoService.save(producto));
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar producto",
            description = "Elimina un producto existente a partir de su ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Producto eliminado correctamente.", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado.", content = @Content),
            @ApiResponse(responseCode = "403", description = "No autorizado para eliminar productos.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado.", content = @Content)
    })
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto que se desea eliminar.", example = "1")
            @PathVariable Long id) {
        Producto producto = productoService.findById(id);
        if (producto != null) {
            productoService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}