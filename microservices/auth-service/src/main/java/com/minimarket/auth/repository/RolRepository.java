package com.minimarket.auth.repository;

import com.minimarket.auth.entity.NombreRol;
import com.minimarket.auth.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(NombreRol nombre);

    boolean existsByNombre(NombreRol nombre);

    List<Rol> findAllByOrderByNombreAsc();
}
