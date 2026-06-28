package com.minimarket.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Categoria;
import com.minimarket.entity.Producto;
import com.minimarket.repository.CategoriaRepository;
import com.minimarket.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventarioAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Long productoId;

    @BeforeEach
    void prepararDatos() {
        String sufijo = UUID.randomUUID().toString();

        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria Inventario Seguridad " + sufijo);
        Categoria categoriaGuardada = (Categoria) categoriaRepository.save(categoria);

        Producto producto = new Producto();
        producto.setNombre("Producto Inventario Seguridad " + sufijo);
        producto.setPrecio(1000.0);
        producto.setStock(50);
        producto.setCategoria(categoriaGuardada);
        Producto productoGuardado = (Producto) productoRepository.save(producto);

        this.productoId = productoGuardado.getId();
    }

    @Test
    void registrarInventarioSinTokenDebeRetornarNoAutorizado() throws Exception {
        mockMvc.perform(post("/api/inventario")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventarioJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoDebeRegistrarInventario() throws Exception {
        String tokenCliente = obtenerToken("cliente", "cliente123");

        mockMvc.perform(post("/api/inventario")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventarioJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void cajeroDebeRegistrarInventario() throws Exception {
        String tokenEmpleado = obtenerToken("empleado", "empleado123");

        mockMvc.perform(post("/api/inventario")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventarioJson()))
                .andExpect(status().isOk());
    }

    @Test
    void administradorDebeRegistrarInventario() throws Exception {
        String tokenGerente = obtenerToken("gerente", "gerente123");

        mockMvc.perform(post("/api/inventario")
                        .header("Authorization", "Bearer " + tokenGerente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inventarioJson()))
                .andExpect(status().isOk());
    }

    private String obtenerToken(String username, String password) throws Exception {
        String loginJson = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(response);
        return jsonNode.get("token").asText();
    }

    private String inventarioJson() {
        return """
                {
                    "producto": {
                        "id": %d
                    },
                    "cantidad": 10,
                    "tipoMovimiento": "Entrada",
                    "fechaMovimiento": "2026-06-27T12:00:00.000+00:00"
                }
                """.formatted(productoId);
    }
}