package com.minimarket.productoinventario.repository;

import com.minimarket.productoinventario.entity.MovimientoInventario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository
        extends JpaRepository<MovimientoInventario, Long> {

    @EntityGraph(attributePaths = "producto")
    List<MovimientoInventario>
            findByProductoIdOrderByFechaMovimientoDesc(Long productoId);

    boolean existsByProductoId(Long productoId);
}
