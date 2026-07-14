package com.minimarket.controller;

import com.minimarket.dto.error.ApiErrorResponse;
import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.security.dto.AuthResponse;
import com.minimarket.security.dto.LoginRequest;
import com.minimarket.security.dto.RegisterRequest;
import com.minimarket.security.jwt.JwtUtil;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/auth")
@Tag(
        name = "Autenticación",
        description = "Operaciones públicas para registrar usuarios e iniciar sesión mediante JWT."
)
public class AuthController {

    private static final String DEFAULT_ROLE = "ROLE_CLIENTE";

    private final UsuarioService usuarioService;
    private final RolRepository rolRepository;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;

    public AuthController(
            UsuarioService usuarioService,
            RolRepository rolRepository,
            AuthenticationManager authenticationManager,
            CustomUserDetailsService customUserDetailsService,
            JwtUtil jwtUtil
    ) {
        this.usuarioService = usuarioService;
        this.rolRepository = rolRepository;
        this.authenticationManager = authenticationManager;
        this.customUserDetailsService = customUserDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    @Operation(
            summary = "Registrar usuario",
            description = """
                    Registra un nuevo usuario con el rol ROLE_CLIENTE.
                    Valida los datos obligatorios y evita nombres de usuario duplicados.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Datos necesarios para registrar un nuevo cliente.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = RegisterRequest.class),
                    examples = @ExampleObject(
                            name = "Registro de cliente",
                            value = """
                                    {
                                      "username": "cliente_nuevo",
                                      "password": "ClaveSegura123",
                                      "nombre": "Camila",
                                      "apellido": "Soto",
                                      "email": "camila.soto@example.com",
                                      "direccion": "Av. Principal 123"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Usuario registrado correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = Map.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "message": "Usuario registrado correctamente",
                                              "username": "cliente_nuevo",
                                              "nombre": "Camila",
                                              "apellido": "Soto",
                                              "email": "camila.soto@example.com",
                                              "direccion": "Av. Principal 123",
                                              "roles": ["ROLE_CLIENTE"]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Faltan datos obligatorios.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El nombre de usuario ya se encuentra registrado.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<Map<String, Object>> register(
            @RequestBody RegisterRequest request
    ) {
        validarRegistro(request);

        if (usuarioService.findByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El username ya existe"
            );
        }

        Rol rolCliente = rolRepository.findByNombre(DEFAULT_ROLE)
                .orElseGet(() -> {
                    Rol nuevoRol = new Rol();
                    nuevoRol.setNombre(DEFAULT_ROLE);
                    return rolRepository.save(nuevoRol);
                });

        Set<Rol> roles = new HashSet<>();
        roles.add(rolCliente);

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(request.getPassword());
        usuario.setNombre(request.getNombre());
        usuario.setApellido(request.getApellido());
        usuario.setEmail(request.getEmail());
        usuario.setDireccion(request.getDireccion());
        usuario.setRoles(roles);

        Usuario usuarioGuardado = usuarioService.save(usuario);

        Map<String, Object> respuesta = Map.of(
                "message", "Usuario registrado correctamente",
                "username", usuarioGuardado.getUsername(),
                "nombre", usuarioGuardado.getNombre(),
                "apellido", usuarioGuardado.getApellido(),
                "email", usuarioGuardado.getEmail(),
                "direccion", usuarioGuardado.getDireccion(),
                "roles", usuarioGuardado.getRoles()
                        .stream()
                        .map(Rol::getNombre)
                        .toList()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }

    @PostMapping("/login")
    @Operation(
            summary = "Iniciar sesión",
            description = """
                    Valida las credenciales y genera un token JWT.
                    El token puede ingresarse en el botón Authorize de Swagger UI.
                    """
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Credenciales del usuario registrado.",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = LoginRequest.class),
                    examples = @ExampleObject(
                            name = "Inicio de sesión",
                            value = """
                                    {
                                      "username": "gerente",
                                      "password": "gerente123"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Autenticación realizada correctamente.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "token": "eyJhbGciOiJIUzI1NiJ9...",
                                              "tokenType": "Bearer",
                                              "username": "gerente",
                                              "roles": ["ROLE_GERENTE"]
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Usuario o contraseña incorrectos.",
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    public ResponseEntity<AuthResponse> login(
            @RequestBody LoginRequest request
    ) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(
                        request.getUsername()
                );

        String token = jwtUtil.generateToken(userDetails);

        AuthResponse response = new AuthResponse(
                token,
                "Bearer",
                userDetails.getUsername(),
                userDetails.getAuthorities()
                        .stream()
                        .map(authority -> authority.getAuthority())
                        .toList()
        );

        return ResponseEntity.ok(response);
    }

    private void validarRegistro(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw badRequest("El username es obligatorio");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw badRequest("La password es obligatoria");
        }

        if (request.getNombre() == null || request.getNombre().isBlank()) {
            throw badRequest("El nombre es obligatorio");
        }

        if (request.getApellido() == null || request.getApellido().isBlank()) {
            throw badRequest("El apellido es obligatorio");
        }

        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw badRequest("El email es obligatorio");
        }

        if (request.getDireccion() == null || request.getDireccion().isBlank()) {
            throw badRequest("La dirección es obligatoria");
        }
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
        );
    }
}