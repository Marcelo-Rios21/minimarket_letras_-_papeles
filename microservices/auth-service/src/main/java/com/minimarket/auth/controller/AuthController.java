package com.minimarket.auth.controller;

import com.minimarket.auth.dto.AuthResponse;
import com.minimarket.auth.dto.LoginRequest;
import com.minimarket.auth.dto.RegisterRequest;
import com.minimarket.auth.dto.UsuarioResponse;
import com.minimarket.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Autenticación",
        description = "Registro público e inicio de sesión"
)
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar cliente",
            description = "Crea un usuario activo con ROLE_CLIENTE"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente registrado"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Username duplicado",
                    content = @Content
            )
    })
    public ResponseEntity<UsuarioResponse> registrar(
            @Valid @RequestBody RegisterRequest request
    ) {
        UsuarioResponse response = authService.registrar(request);

        URI location = URI.create(
                "/api/usuarios/" + response.id()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales y entrega un JWT"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación exitosa",
                    content = @Content(
                            schema = @Schema(
                                    implementation =
                                            AuthResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos",
                    content = @Content
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciales inválidas",
                    content = @Content
            )
    })
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(
                authService.autenticar(request)
        );
    }
}
