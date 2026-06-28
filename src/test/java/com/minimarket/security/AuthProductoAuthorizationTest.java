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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthProductoAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    private Long productoId;
    private Long categoriaId;
    private String categoriaNombre;

    @BeforeEach
    void prepararDatos() {
        String sufijo = UUID.randomUUID().toString();

        Categoria categoria = new Categoria();
        categoria.setNombre("Categoria Seguridad " + sufijo);
        Categoria categoriaGuardada = (Categoria) categoriaRepository.save(categoria);

        Producto producto = new Producto();
        producto.setNombre("Producto Seguridad " + sufijo);
        producto.setPrecio(1000.0);
        producto.setStock(25);
        producto.setCategoria(categoriaGuardada);
        Producto productoGuardado = (Producto) productoRepository.save(producto);

        this.categoriaId = categoriaGuardada.getId();
        this.categoriaNombre = categoriaGuardada.getNombre();
        this.productoId = productoGuardado.getId();
    }

    @Test
    void loginValidoDebeRetornarTokenJwt() throws Exception {
        String loginJson = """
                {
                    "username": "empleado",
                    "password": "empleado123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("empleado"));
    }

    @Test
    void loginInvalidoDebeRetornarNoAutorizado() throws Exception {
        String loginJson = """
                {
                    "username": "empleado",
                    "password": "password_incorrecta"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void modificarProductoSinTokenDebeRetornarNoAutorizado() throws Exception {
        mockMvc.perform(put("/api/productos/" + productoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoDebeModificarProducto() throws Exception {
        String tokenCliente = obtenerToken("cliente", "cliente123");

        mockMvc.perform(put("/api/productos/" + productoId)
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void cajeroNoDebeModificarProducto() throws Exception {
        String tokenEmpleado = obtenerToken("empleado", "empleado123");

        mockMvc.perform(put("/api/productos/" + productoId)
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorDebeModificarProducto() throws Exception {
        String tokenGerente = obtenerToken("gerente", "gerente123");

        mockMvc.perform(put("/api/productos/" + productoId)
                        .header("Authorization", "Bearer " + tokenGerente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson()))
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

    private String productoJson() {
        return """
                {
                    "nombre": "Producto actualizado seguridad",
                    "precio": 1500.0,
                    "stock": 30,
                    "categoria": {
                        "id": %d,
                        "nombre": "%s"
                    }
                }
                """.formatted(categoriaId, categoriaNombre);
    }
}