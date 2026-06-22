package com.minimarket.service.impl;

import com.minimarket.entity.Inventario;
import com.minimarket.repository.InventarioRepository;
import com.minimarket.service.InventarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventarioServiceImpl implements InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    @Override
    public List<Inventario> findAll() {
        return inventarioRepository.findAll();
    }

    @Override
    public Inventario findById(Long id) {
        return inventarioRepository.findById(id).orElse(null);
    }

    @Override
    public Inventario save(Inventario inventario) {
        return registrarMovimiento(inventario);
    }

    @Override
    public void deleteById(Long id) {
        inventarioRepository.deleteById(id);
    }

    @Override
    public List<Inventario> findByProductoId(Long productoId) {
        return inventarioRepository.findByProductoId(productoId);
    }

    @Override
    public Inventario registrarMovimiento(Inventario inventario) {
        validarMovimiento(inventario);

        String tipoMovimientoNormalizado = inventario.getTipoMovimiento().trim();
        inventario.setTipoMovimiento(tipoMovimientoNormalizado);

        return inventarioRepository.save(inventario);
    }

    private void validarMovimiento(Inventario inventario) {
        if (inventario == null) {
            throw new IllegalArgumentException("El movimiento de inventario es obligatorio");
        }

        if (inventario.getProducto() == null) {
            throw new IllegalArgumentException("El producto asociado al inventario es obligatorio");
        }

        if (inventario.getTipoMovimiento() == null || inventario.getTipoMovimiento().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de movimiento es obligatorio");
        }

        if (!esTipoMovimientoValido(inventario.getTipoMovimiento())) {
            throw new IllegalArgumentException("El tipo de movimiento debe ser Entrada o Salida");
        }

        if (inventario.getCantidad() == null || inventario.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
    }

    private boolean esTipoMovimientoValido(String tipoMovimiento) {
        String valor = tipoMovimiento.trim();
        return valor.equalsIgnoreCase("Entrada") || valor.equalsIgnoreCase("Salida");
    }
}