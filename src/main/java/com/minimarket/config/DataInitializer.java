package com.minimarket.config;

import com.minimarket.entity.Rol;
import com.minimarket.entity.Usuario;
import com.minimarket.repository.RolRepository;
import com.minimarket.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            RolRepository rolRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Rol rolCliente = crearRolSiNoExiste("ROLE_CLIENTE");
        Rol rolEmpleado = crearRolSiNoExiste("ROLE_EMPLEADO");
        Rol rolGerente = crearRolSiNoExiste("ROLE_GERENTE");

        crearUsuarioSiNoExiste("cliente", "cliente123", rolCliente);
        crearUsuarioSiNoExiste("empleado", "empleado123", rolEmpleado);
        crearUsuarioSiNoExiste("gerente", "gerente123", rolGerente);
    }

    private Rol crearRolSiNoExiste(String nombreRol) {
        return rolRepository.findByNombre(nombreRol)
                .orElseGet(() -> {
                    Rol rol = new Rol();
                    rol.setNombre(nombreRol);
                    return rolRepository.save(rol);
                });
    }

    private void crearUsuarioSiNoExiste(String username, String password, Rol rol) {
        boolean usuarioExiste = usuarioRepository.findByUsername(username).isPresent();

        if (!usuarioExiste) {
            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPassword(passwordEncoder.encode(password));

            Set<Rol> roles = new HashSet<>();
            roles.add(rol);
            usuario.setRoles(roles);

            usuarioRepository.save(usuario);
        }
    }
}