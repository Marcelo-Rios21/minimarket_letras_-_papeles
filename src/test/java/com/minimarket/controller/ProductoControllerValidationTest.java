package com.minimarket.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Categoria;
import com.minimarket.repository.CategoriaRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductoControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Long categoriaId;
    private String tokenGerente;

    @BeforeEach
    void prepararDatos() throws Exception {
        Categoria categoria = new Categoria();
        categoria.setNombre(
                "Categoria Validacion " + UUID.randomUUID()
        );

        Categoria categoriaGuardada = categoriaRepository.save(categoria);

        this.categoriaId = categoriaGuardada.getId();
        this.tokenGerente = obtenerToken("gerente", "gerente123");
    }

    @Test
    void crearProductoConDatosValidosDebeRetornarCreated() throws Exception {
        mockMvc.perform(post("/api/productos")
                        .header(
                                "Authorization",
                                "Bearer " + tokenGerente
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson(
                                "Cuaderno validado",
                                2490.0,
                                40
                        )))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre")
                        .value("Cuaderno validado"))
                .andExpect(jsonPath("$.precio").value(2490.0))
                .andExpect(jsonPath("$.stock").value(40));
    }

    @Test
    void crearProductoConPrecioNegativoDebeRetornarBadRequest()
            throws Exception {

        mockMvc.perform(post("/api/productos")
                        .header(
                                "Authorization",
                                "Bearer " + tokenGerente
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson(
                                "Producto invalido",
                                -100.0,
                                10
                        )))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error")
                        .value("Solicitud inválida"))
                .andExpect(jsonPath("$.message")
                        .value("Uno o más campos contienen errores."))
                .andExpect(jsonPath("$.path")
                        .value("/api/productos"))
                .andExpect(jsonPath("$.fieldErrors.precio")
                        .value(
                                "El precio del producto debe ser mayor que cero"
                        ));
    }

    @Test
    void obtenerProductoInexistenteDebeRetornarErrorGlobal()
            throws Exception {

        long idInexistente = Long.MAX_VALUE;

        mockMvc.perform(get("/api/productos/" + idInexistente)
                        .header(
                                "Authorization",
                                "Bearer " + tokenGerente
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error")
                        .value("Recurso no encontrado"))
                .andExpect(jsonPath("$.message")
                        .value(containsString(
                                "No existe un producto con ID"
                        )))
                .andExpect(jsonPath("$.path")
                        .value("/api/productos/" + idInexistente));
    }

    private String obtenerToken(
            String username,
            String password
    ) throws Exception {

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

    private String productoJson(
            String nombre,
            Double precio,
            Integer stock
    ) {
        return """
                {
                    "nombre": "%s",
                    "precio": %s,
                    "stock": %d,
                    "categoria": {
                        "id": %d
                    }
                }
                """.formatted(
                nombre,
                precio,
                stock,
                categoriaId
        );
    }
}