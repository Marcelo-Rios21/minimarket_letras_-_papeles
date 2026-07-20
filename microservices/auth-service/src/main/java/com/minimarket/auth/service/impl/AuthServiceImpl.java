package com.minimarket.auth.service.impl;

import com.minimarket.auth.dto.AuthResponse;
import com.minimarket.auth.dto.LoginRequest;
import com.minimarket.auth.dto.RegisterRequest;
import com.minimarket.auth.dto.UsuarioResponse;
import com.minimarket.auth.mapper.UsuarioMapper;
import com.minimarket.auth.security.JwtService;
import com.minimarket.auth.service.AuthService;
import com.minimarket.auth.service.UsuarioService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final JwtService jwtService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper,
            JwtService jwtService
    ) {
        this.authenticationManager = authenticationManager;
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
        this.jwtService = jwtService;
    }

    @Override
    public UsuarioResponse registrar(RegisterRequest request) {
        return usuarioMapper.toResponse(
                usuarioService.registrarCliente(request)
        );
    }

    @Override
    public AuthResponse autenticar(LoginRequest request) {
        String username = request.username()
                .trim()
                .toLowerCase(Locale.ROOT);

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                username,
                                request.password()
                        )
                );

        if (!(authentication.getPrincipal()
                instanceof UserDetails userDetails)) {
            throw new IllegalStateException(
                    "No fue posible obtener el usuario autenticado"
            );
        }

        List<String> roles = userDetails.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();

        String token = jwtService.generarToken(userDetails);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.obtenerExpiracionSegundos(),
                userDetails.getUsername(),
                roles
        );
    }
}
