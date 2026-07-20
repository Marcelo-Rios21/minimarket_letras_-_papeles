package com.minimarket.auth.controller;

import com.minimarket.auth.assembler.UsuarioModelAssembler;
import com.minimarket.auth.dto.UsuarioAdminRequest;
import com.minimarket.auth.dto.UsuarioResponse;
import com.minimarket.auth.dto.UsuarioUpdateRequest;
import com.minimarket.auth.entity.Usuario;
import com.minimarket.auth.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/usuarios")
@Tag(
        name = "Usuarios",
        description = "Administración de usuarios y roles"
)
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioModelAssembler usuarioAssembler;

    public UsuarioController(
            UsuarioService usuarioService,
            UsuarioModelAssembler usuarioAssembler
    ) {
        this.usuarioService = usuarioService;
        this.usuarioAssembler = usuarioAssembler;
    }

    @GetMapping
    @Operation(summary = "Listar usuarios")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Usuarios encontrados"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Rol no autorizado"
            )
    })
    public CollectionModel<EntityModel<UsuarioResponse>> listar() {
        List<EntityModel<UsuarioResponse>> usuarios =
                usuarioService.listarUsuarios()
                        .stream()
                        .map(usuarioAssembler::toModel)
                        .toList();

        return CollectionModel.of(
                usuarios,
                linkTo(
                        methodOn(UsuarioController.class)
                                .listar()
                ).withSelfRel()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar usuario por ID")
    public EntityModel<UsuarioResponse> buscarPorId(
            @PathVariable Long id
    ) {
        return usuarioAssembler.toModel(
                usuarioService.buscarPorId(id)
        );
    }

    @PostMapping
    @Operation(summary = "Crear usuario administrativo")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario creado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username duplicado"
            )
    })
    public ResponseEntity<EntityModel<UsuarioResponse>> crear(
            @Valid @RequestBody UsuarioAdminRequest request
    ) {
        Usuario usuario = usuarioService.crearUsuario(request);

        EntityModel<UsuarioResponse> model =
                usuarioAssembler.toModel(usuario);

        URI location = model
                .getRequiredLink(IanaLinkRelations.SELF)
                .toUri();

        return ResponseEntity
                .created(location)
                .body(model);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public EntityModel<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateRequest request
    ) {
        return usuarioAssembler.toModel(
                usuarioService.actualizarUsuario(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Desactivar usuario",
            description = "Realiza una eliminación lógica"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Usuario desactivado"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Usuario inexistente"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Usuario ya desactivado"
            )
    })
    public ResponseEntity<Void> desactivar(
            @PathVariable Long id
    ) {
        usuarioService.desactivarUsuario(id);
        return ResponseEntity.noContent().build();
    }
}
