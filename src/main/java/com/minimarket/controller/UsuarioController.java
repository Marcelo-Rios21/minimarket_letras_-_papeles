package com.minimarket.controller;

import com.minimarket.dto.error.ApiErrorResponse;
import com.minimarket.entity.Usuario;
import com.minimarket.exception.ResourceNotFoundException;
import com.minimarket.hateoas.UsuarioModelAssembler;
import com.minimarket.service.UsuarioService;
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
@RequestMapping("/api/usuarios")
@Tag(
        name = "Usuarios",
        description = """
                Administración de usuarios y roles del sistema.
                Todas las operaciones están restringidas al rol GERENTE.
                """
)
@SecurityRequirement(name = SECURITY_SCHEME_NAME)
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioModelAssembler usuarioModelAssembler;

    public UsuarioController(
            UsuarioService usuarioService,
            UsuarioModelAssembler usuarioModelAssembler
    ) {
        this.usuarioService = usuarioService;
        this.usuarioModelAssembler = usuarioModelAssembler;
    }

    @GetMapping
    @Operation(
            summary = "Listar usuarios",
            description = "Obtiene todos los usuarios registrados en el sistema."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuarios obtenidos correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(
                                    schema = @Schema(
                                            implementation = Usuario.class
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
                    description = "La operación requiere el rol GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public CollectionModel<EntityModel<Usuario>> listarUsuarios() {
        List<EntityModel<Usuario>> usuarios =
                usuarioService.findAll()
                        .stream()
                        .map(usuarioModelAssembler::toModel)
                        .toList();

        return CollectionModel.of(
                usuarios,
                linkTo(
                        methodOn(UsuarioController.class)
                                .listarUsuarios()
                ).withSelfRel()
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener usuario por ID",
            description = "Busca un usuario mediante su identificador único."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario encontrado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = Usuario.class
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
                    description = "La operación requiere el rol GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El usuario solicitado no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<Usuario>> obtenerUsuarioPorId(
            @Parameter(
                    description = "Identificador del usuario.",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        Usuario usuario = buscarUsuario(id);

        return ResponseEntity.ok(
                usuarioModelAssembler.toModel(usuario)
        );
    }

    @PostMapping
    @Operation(
            summary = "Crear usuario",
            description = """
                    Crea un usuario y asigna los roles enviados.
                    La contraseña es cifrada mediante BCrypt antes de almacenarse.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Datos del usuario que será creado.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Usuario.class),
                    examples = @ExampleObject(
                            name = "Nuevo usuario",
                            value = """
                                    {
                                      "username": "nuevo.empleado",
                                      "password": "ClaveSegura123",
                                      "nombre": "Daniel",
                                      "apellido": "Pérez",
                                      "email": "daniel.perez@example.com",
                                      "direccion": "Av. Central 450",
                                      "roles": [
                                        {
                                          "id": 2,
                                          "nombre": "ROLE_EMPLEADO"
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
                    description = "Usuario creado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = Usuario.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La solicitud contiene datos inválidos.",
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
                    description = "La operación requiere el rol GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El nombre de usuario ya está registrado.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<Usuario>> guardarUsuario(
            @RequestBody Usuario usuario
    ) {
        Usuario usuarioGuardado = usuarioService.save(usuario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        usuarioModelAssembler.toModel(
                                usuarioGuardado
                        )
                );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar usuario",
            description = """
                    Reemplaza los datos de un usuario existente.
                    La solicitud debe incluir la contraseña que será almacenada cifrada.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Datos actualizados del usuario.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = Usuario.class),
                    examples = @ExampleObject(
                            name = "Usuario actualizado",
                            value = """
                                    {
                                      "username": "empleado.actualizado",
                                      "password": "NuevaClave123",
                                      "nombre": "Daniel",
                                      "apellido": "Pérez",
                                      "email": "daniel.actualizado@example.com",
                                      "direccion": "Av. Central 500",
                                      "roles": [
                                        {
                                          "id": 2,
                                          "nombre": "ROLE_EMPLEADO"
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuario actualizado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(
                                    implementation = Usuario.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "La solicitud contiene datos inválidos.",
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
                    description = "La operación requiere el rol GERENTE.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El usuario solicitado no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El nombre de usuario pertenece a otro registro.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<EntityModel<Usuario>> actualizarUsuario(
            @Parameter(
                    description = "Identificador del usuario.",
                    example = "1"
            )
            @PathVariable Long id,
            @RequestBody Usuario usuario
    ) {
        buscarUsuario(id);
        usuario.setId(id);

        Usuario usuarioActualizado =
                usuarioService.save(usuario);

        return ResponseEntity.ok(
                usuarioModelAssembler.toModel(
                        usuarioActualizado
                )
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar usuario",
            description = "Elimina un usuario existente mediante su ID."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario eliminado correctamente.",
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
                    description = "El usuario solicitado no existe.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El usuario mantiene relaciones que impiden eliminarlo.",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    public ResponseEntity<Void> eliminarUsuario(
            @Parameter(
                    description = "Identificador del usuario.",
                    example = "1"
            )
            @PathVariable Long id
    ) {
        buscarUsuario(id);
        usuarioService.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioService.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un usuario con ID " + id
                ));
    }
}