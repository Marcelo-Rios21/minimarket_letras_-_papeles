package com.minimarket.controller;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.security.dto.AuthResponse;
import com.minimarket.security.dto.LoginRequest;
import com.minimarket.security.dto.RegisterRequest;
import com.minimarket.security.jwt.JwtUtil;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.service.UsuarioService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
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
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {

        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return ResponseEntity.badRequest().body("El username es obligatorio");
        }

        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("La password es obligatoria");
        }

        if (usuarioService.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("El username ya existe");
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
        usuario.setRoles(roles);

        Usuario usuarioGuardado = usuarioService.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Usuario registrado correctamente",
                "username", usuarioGuardado.getUsername(),
                "roles", usuarioGuardado.getRoles().stream()
                        .map(Rol::getNombre)
                        .toList()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(request.getUsername());

        String token = jwtUtil.generateToken(userDetails);

        AuthResponse response = new AuthResponse(
                token,
                "Bearer",
                userDetails.getUsername(),
                userDetails.getAuthorities().stream()
                        .map(authority -> authority.getAuthority())
                        .toList()
        );

        return ResponseEntity.ok(response);
    }
}