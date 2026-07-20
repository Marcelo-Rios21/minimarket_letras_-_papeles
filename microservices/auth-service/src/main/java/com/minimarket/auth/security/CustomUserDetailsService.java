package com.minimarket.auth.security;

import com.minimarket.auth.entity.Rol;
import com.minimarket.auth.entity.Usuario;
import com.minimarket.auth.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(
            UsuarioRepository usuarioRepository
    ) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        String usernameNormalizado = normalizarUsername(username);

        Usuario usuario = usuarioRepository
                .findByUsernameIgnoreCase(usernameNormalizado)
                .filter(Usuario::isActivo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Credenciales inválidas"
                ));

        List<SimpleGrantedAuthority> authorities =
                usuario.getRoles()
                        .stream()
                        .map(Rol::getNombre)
                        .map(Enum::name)
                        .sorted()
                        .map(SimpleGrantedAuthority::new)
                        .toList();

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .disabled(!usuario.isActivo())
                .build();
    }

    private String normalizarUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException(
                    "Credenciales inválidas"
            );
        }

        return username.trim().toLowerCase(Locale.ROOT);
    }
}
