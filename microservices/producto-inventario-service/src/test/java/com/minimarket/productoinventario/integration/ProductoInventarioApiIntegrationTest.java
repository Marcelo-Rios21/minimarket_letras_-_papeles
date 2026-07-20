package com.minimarket.productoinventario.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class ProductoInventarioApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void openApi_estaDisponibleYDescribeElMicroservicio()
            throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.info.title")
                                .value(
                                        "API de Productos e Inventario"
                                )
                )
                .andExpect(
                        jsonPath("$.info.version")
                                .value("1.0.0")
                )
                .andExpect(
                        jsonPath("$.paths['/api/categorias']")
                                .exists()
                )
                .andExpect(
                        jsonPath("$.paths['/api/productos']")
                                .exists()
                )
                .andExpect(
                        jsonPath(
                                "$.paths['/api/productos/"
                                        + "{productoId}/movimientos']"
                        ).exists()
                );
    }

    @Test
    void flujoCompleto_creaCategoriaProductoYEntradaActualizaStock()
            throws Exception {
        Long categoriaId = crearCategoria("Lácteos");

        Long productoId = crearProducto(
                "Leche entera",
                "1290.00",
                categoriaId
        );

        mockMvc.perform(
                        post(
                                "/api/productos/{productoId}/movimientos",
                                productoId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "tipoMovimiento": "ENTRADA",
                                          "cantidad": 10
                                        }
                                        """
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.productoId")
                                .value(productoId)
                )
                .andExpect(
                        jsonPath("$.productoNombre")
                                .value("Leche entera")
                )
                .andExpect(
                        jsonPath("$.tipoMovimiento")
                                .value("ENTRADA")
                )
                .andExpect(jsonPath("$.cantidad").value(10))
                .andExpect(
                        jsonPath("$._links.producto.href")
                                .exists()
                )
                .andExpect(
                        jsonPath("$._links.historial.href")
                                .exists()
                );

        mockMvc.perform(
                        get(
                                "/api/productos/{id}",
                                productoId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(10))
                .andExpect(
                        jsonPath("$.categoriaId")
                                .value(categoriaId)
                )
                .andExpect(
                        jsonPath("$._links.self.href")
                                .exists()
                )
                .andExpect(
                        jsonPath("$._links.movimientos.href")
                                .exists()
                );
    }

    @Test
    void crearProducto_conStockEnviado_loIgnoraYUsaCero()
            throws Exception {
        Long categoriaId = crearCategoria("Bebidas");

        mockMvc.perform(
                        post("/api/productos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "nombre": "Jugo natural",
                                          "precio": 1990.00,
                                          "categoriaId": %d,
                                          "stock": 500
                                        }
                                        """.formatted(categoriaId)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stock").value(0))
                .andExpect(header().exists("Location"));
    }

    @Test
    void salidaMayorAlStock_retorna409YConservaElStock()
            throws Exception {
        Long categoriaId = crearCategoria("Abarrotes");

        Long productoId = crearProducto(
                "Arroz",
                "1490.00",
                categoriaId
        );

        registrarMovimiento(
                productoId,
                "ENTRADA",
                3,
                201
        );

        mockMvc.perform(
                        post(
                                "/api/productos/{productoId}/movimientos",
                                productoId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "tipoMovimiento": "SALIDA",
                                          "cantidad": 5
                                        }
                                        """
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Stock insuficiente para el "
                                                + "producto "
                                                + productoId
                                                + ". Stock disponible: 3"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value(
                                        "/api/productos/"
                                                + productoId
                                                + "/movimientos"
                                )
                );

        mockMvc.perform(
                        get(
                                "/api/productos/{id}",
                                productoId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(3));
    }

    @Test
    void categoriaConNombreVacio_retorna400ConDetalle()
            throws Exception {
        mockMvc.perform(
                        post("/api/categorias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "nombre": "   "
                                        }
                                        """
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "La solicitud contiene "
                                                + "datos inválidos"
                                )
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors[0].field"
                        ).value("nombre")
                )
                .andExpect(
                        jsonPath(
                                "$.validationErrors[0].message"
                        ).value(
                                "El nombre de la categoría "
                                        + "es obligatorio"
                        )
                );
    }

    @Test
    void obtenerProductoInexistente_retorna404Estructurado()
            throws Exception {
        mockMvc.perform(get("/api/productos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(
                        jsonPath("$.error")
                                .value("Not Found")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No existe un producto "
                                                + "con ID 999"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/productos/999")
                );
    }

    private Long crearCategoria(String nombre) throws Exception {
        String response = mockMvc.perform(
                        post("/api/categorias")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "nombre": "%s"
                                        }
                                        """.formatted(nombre)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(
                        jsonPath("$._links.self.href")
                                .exists()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private Long crearProducto(
            String nombre,
            String precio,
            Long categoriaId
    ) throws Exception {
        String response = mockMvc.perform(
                        post("/api/productos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "nombre": "%s",
                                          "precio": %s,
                                          "categoriaId": %d
                                        }
                                        """.formatted(
                                                nombre,
                                                precio,
                                                categoriaId
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.stock").value(0))
                .andExpect(
                        jsonPath("$._links.categoria.href")
                                .exists()
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private void registrarMovimiento(
            Long productoId,
            String tipo,
            int cantidad,
            int estadoEsperado
    ) throws Exception {
        mockMvc.perform(
                        post(
                                "/api/productos/{productoId}/movimientos",
                                productoId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "tipoMovimiento": "%s",
                                          "cantidad": %d
                                        }
                                        """.formatted(
                                                tipo,
                                                cantidad
                                        )
                                )
                )
                .andExpect(status().is(estadoEsperado));
    }
}
