package com.minimarket.controller;

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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HateoasIntegrationTest {

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
    private String tokenGerente;

    @BeforeEach
    void prepararDatos() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNombre(
                "Categoria HATEOAS " + UUID.randomUUID()
        );

        Categoria categoriaGuardada =
                categoriaRepository.save(categoria);

        Producto producto = new Producto();
        producto.setNombre(
                "Producto HATEOAS " + UUID.randomUUID()
        );
        producto.setPrecio(1990.0);
        producto.setStock(20);
        producto.setCategoria(categoriaGuardada);

        Producto productoGuardado =
                productoRepository.save(producto);

        categoriaId = categoriaGuardada.getId();
        productoId = productoGuardado.getId();
        tokenGerente = obtenerToken();
    }

    @Test
    void obtenerProductoDebeIncluirEnlacesHateoas()
            throws Exception {

        mockMvc.perform(
                        get("/api/productos/" + productoId)
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenGerente
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productoId))
                .andExpect(
                        jsonPath("$._links.self.href")
                                .value(containsString(
                                        "/api/productos/" + productoId
                                ))
                )
                .andExpect(
                        jsonPath("$._links.productos.href")
                                .value(containsString(
                                        "/api/productos"
                                ))
                )
                .andExpect(
                        jsonPath("$._links.categoria.href")
                                .value(containsString(
                                        "/api/categorias/" + categoriaId
                                ))
                );
    }

    @Test
    void listarProductosDebeUsarCollectionModel()
            throws Exception {

        mockMvc.perform(
                        get("/api/productos")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenGerente
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._embedded").exists())
                .andExpect(
                        jsonPath("$._links.self.href")
                                .value(containsString(
                                        "/api/productos"
                                ))
                );
    }

    private String obtenerToken() throws Exception {
        String loginJson = """
                {
                  "username": "gerente",
                  "password": "gerente123"
                }
                """;

        String respuesta = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(loginJson)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(respuesta);

        return json.get("token").asText();
    }
}