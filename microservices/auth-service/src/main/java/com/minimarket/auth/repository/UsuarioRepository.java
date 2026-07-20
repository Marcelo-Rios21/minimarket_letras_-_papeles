package com.minimarket.auth.repository;

import com.minimarket.auth.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Long> {

    @Override
    @EntityGraph(attributePaths = "roles")
    List<Usuario> findAll();

    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findById(Long id);

    @EntityGraph(attributePaths = "roles")
    Optional<Usuario> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndIdNot(
            String username,
            Long id
    );
}
