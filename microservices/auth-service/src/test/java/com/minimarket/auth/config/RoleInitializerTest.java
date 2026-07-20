package com.minimarket.auth.config;

import com.minimarket.auth.entity.NombreRol;
import com.minimarket.auth.entity.Rol;
import com.minimarket.auth.repository.RolRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleInitializerTest {

    @Mock
    private RolRepository rolRepository;

    @Test
    void creaLosTresRolesCuandoNoExisten() {
        when(rolRepository.existsByNombre(
                any(NombreRol.class)
        )).thenReturn(false);

        RoleInitializer initializer = new RoleInitializer(
                rolRepository
        );

        initializer.run(null);

        ArgumentCaptor<Rol> captor =
                ArgumentCaptor.forClass(Rol.class);

        verify(
                rolRepository,
                times(NombreRol.values().length)
        ).save(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(Rol::getNombre)
                .containsExactlyInAnyOrder(
                        NombreRol.values()
                );
    }

    @Test
    void noDuplicaRolesQueYaExisten() {
        when(rolRepository.existsByNombre(
                any(NombreRol.class)
        )).thenReturn(true);

        RoleInitializer initializer = new RoleInitializer(
                rolRepository
        );

        initializer.run(null);

        verify(rolRepository, never())
                .save(any(Rol.class));
    }
}
