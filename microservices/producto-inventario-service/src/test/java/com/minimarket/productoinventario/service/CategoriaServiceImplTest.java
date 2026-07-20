package com.minimarket.productoinventario.service;

import com.minimarket.productoinventario.entity.Categoria;
import com.minimarket.productoinventario.exception.BusinessConflictException;
import com.minimarket.productoinventario.exception.ResourceNotFoundException;
import com.minimarket.productoinventario.repository.CategoriaRepository;
import com.minimarket.productoinventario.repository.ProductoRepository;
import com.minimarket.productoinventario.service.impl.CategoriaServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceImplTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaServiceImpl categoriaService;

    @Test
    void listar_retornaCategoriasRegistradas() {
        Categoria categoria = crearCategoria(1L, "Lácteos");

        when(categoriaRepository.findAll())
                .thenReturn(List.of(categoria));

        List<Categoria> resultado = categoriaService.listar();

        assertEquals(1, resultado.size());
        assertSame(categoria, resultado.get(0));
        verify(categoriaRepository).findAll();
    }

    @Test
    void obtenerPorId_conCategoriaExistente_retornaCategoria() {
        Categoria categoria = crearCategoria(1L, "Lácteos");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        Categoria resultado = categoriaService.obtenerPorId(1L);

        assertSame(categoria, resultado);
        verify(categoriaRepository).findById(1L);
    }

    @Test
    void obtenerPorId_conCategoriaInexistente_lanzaExcepcion() {
        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> categoriaService.obtenerPorId(99L)
        );

        assertEquals(
                "No existe una categoría con ID 99",
                exception.getMessage()
        );
    }

    @Test
    void crear_conDatosValidos_normalizaYGuardaCategoria() {
        Categoria categoria = crearCategoria(50L, "  Lácteos  ");

        when(categoriaRepository.existsByNombreIgnoreCase("Lácteos"))
                .thenReturn(false);

        when(categoriaRepository.save(categoria))
                .thenReturn(categoria);

        Categoria resultado = categoriaService.crear(categoria);

        assertSame(categoria, resultado);
        assertEquals("Lácteos", resultado.getNombre());
        assertNull(resultado.getId());

        verify(categoriaRepository)
                .existsByNombreIgnoreCase("Lácteos");

        verify(categoriaRepository).save(categoria);
    }

    @Test
    void crear_conNombreDuplicado_lanzaConflicto() {
        Categoria categoria = crearCategoria(null, "Lácteos");

        when(categoriaRepository.existsByNombreIgnoreCase("Lácteos"))
                .thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> categoriaService.crear(categoria)
        );

        assertEquals(
                "Ya existe una categoría con el nombre Lácteos",
                exception.getMessage()
        );

        verify(categoriaRepository, never()).save(categoria);
    }

    @Test
    void crear_conCategoriaNula_lanzaExcepcion() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoriaService.crear(null)
        );

        assertEquals(
                "La categoría es obligatoria",
                exception.getMessage()
        );

        verifyNoInteractions(
                categoriaRepository,
                productoRepository
        );
    }

    @Test
    void actualizar_conDatosValidos_modificaCategoriaExistente() {
        Categoria existente = crearCategoria(1L, "Abarrotes");
        Categoria cambios = crearCategoria(null, "  Alimentos  ");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        when(
                categoriaRepository
                        .existsByNombreIgnoreCaseAndIdNot(
                                "Alimentos",
                                1L
                        )
        ).thenReturn(false);

        when(categoriaRepository.save(existente))
                .thenReturn(existente);

        Categoria resultado =
                categoriaService.actualizar(1L, cambios);

        assertSame(existente, resultado);
        assertEquals("Alimentos", resultado.getNombre());

        verify(categoriaRepository).save(existente);
    }

    @Test
    void actualizar_conNombreDeOtraCategoria_lanzaConflicto() {
        Categoria existente = crearCategoria(1L, "Abarrotes");
        Categoria cambios = crearCategoria(null, "Lácteos");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(existente));

        when(
                categoriaRepository
                        .existsByNombreIgnoreCaseAndIdNot(
                                "Lácteos",
                                1L
                        )
        ).thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> categoriaService.actualizar(1L, cambios)
        );

        assertEquals(
                "Ya existe otra categoría con el nombre Lácteos",
                exception.getMessage()
        );

        verify(categoriaRepository, never()).save(existente);
    }

    @Test
    void eliminar_sinProductosAsociados_eliminaCategoria() {
        Categoria categoria = crearCategoria(1L, "Lácteos");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.existsByCategoriaId(1L))
                .thenReturn(false);

        categoriaService.eliminar(1L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminar_conProductosAsociados_lanzaConflicto() {
        Categoria categoria = crearCategoria(1L, "Lácteos");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.existsByCategoriaId(1L))
                .thenReturn(true);

        BusinessConflictException exception = assertThrows(
                BusinessConflictException.class,
                () -> categoriaService.eliminar(1L)
        );

        assertEquals(
                "No se puede eliminar la categoría porque tiene "
                        + "productos asociados",
                exception.getMessage()
        );

        verify(categoriaRepository, never()).delete(categoria);
    }

    private Categoria crearCategoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        return categoria;
    }
}
