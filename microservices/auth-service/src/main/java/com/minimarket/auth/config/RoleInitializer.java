package com.minimarket.auth.config;

import com.minimarket.auth.entity.NombreRol;
import com.minimarket.auth.entity.Rol;
import com.minimarket.auth.repository.RolRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RoleInitializer implements ApplicationRunner {

    private final RolRepository rolRepository;

    public RoleInitializer(RolRepository rolRepository) {
        this.rolRepository = rolRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (NombreRol nombreRol : NombreRol.values()) {
            if (!rolRepository.existsByNombre(nombreRol)) {
                rolRepository.save(new Rol(nombreRol));
            }
        }
    }
}
