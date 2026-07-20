package com.minimarket.productoinventario.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Rollback
class ProductoInventarioJwtSecurityIntegrationTest {

    private static final String JWT_SECRET =
            "VGhpcy1pcy1hLXRlc3Qta2V5LWZvci1hdXRo"
                    + "LXNlcnZpY2UtMTIzNDU2";

    private static final String JWT_ISSUER =
            "minimarket-auth-service";

    private final SecretKey secretKey =
            Keys.hmacShaKeyFor(
                    Decoders.BASE64.decode(JWT_SECRET)
            );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void solicitudSinToken_retorna401Estructurado()
            throws Exception {
        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(
                        jsonPath("$.error")
                                .value("Unauthorized")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Se requiere un token JWT válido "
                                                + "para acceder al recurso"
                                )
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/api/productos")
                )
                .andExpect(
                        jsonPath("$.validationErrors")
                                .isEmpty()
                );
    }

    @Test
    void tokenConIssuerIncorrecto_retorna401()
            throws Exception {
        String token = generarToken(
                "cliente",
                List.of("ROLE_CLIENTE"),
                "otro-emisor",
                Instant.now().plusSeconds(3600)
        );

        mockMvc.perform(
                        get("/api/productos")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tokenExpirado_retorna401()
            throws Exception {
        String token = generarToken(
                "cliente",
                List.of("ROLE_CLIENTE"),
                JWT_ISSUER,
                Instant.now().minusSeconds(60)
        );

        mockMvc.perform(
                        get("/api/productos")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clientePuedeConsultarProductosYCategorias()
            throws Exception {
        String token = tokenCliente();

        mockMvc.perform(
                        get("/api/productos")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/api/categorias")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void clienteNoPuedeModificarProductos()
            throws Exception {
        String token = tokenCliente();

        mockMvc.perform(
                        post("/api/productos")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "nombre": "Producto bloqueado",
                                          "precio": 1000,
                                          "categoriaId": 1
                                        }
                                        """
                                )
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        put("/api/productos/999")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "nombre": "Producto bloqueado",
                                          "precio": 1000,
                                          "categoriaId": 1
                                        }
                                        """
                                )
                )
                .andExpect(status().isForbidden());

        mockMvc.perform(
                        delete("/api/productos/999")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void empleadoPuedeCrearYActualizarCategoriaPeroNoEliminarla()
            throws Exception {
        String token = tokenEmpleado();

        Long categoriaId = crearCategoria(
                "Categoría empleado",
                token
        );

        mockMvc.perform(
                        put(
                                "/api/categorias/{id}",
                                categoriaId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "nombre": "Categoría actualizada"
                                        }
                                        """
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.nombre")
                                .value("Categoría actualizada")
                );

        mockMvc.perform(
                        delete(
                                "/api/categorias/{id}",
                                categoriaId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void empleadoNoPuedeCrearProductos()
            throws Exception {
        String token = tokenEmpleado();

        mockMvc.perform(
                        post("/api/productos")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "nombre": "Producto empleado",
                                          "precio": 1000,
                                          "categoriaId": 1
                                        }
                                        """
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void empleadoPuedeRegistrarYConsultarMovimientos()
            throws Exception {
        String gerenteToken = tokenGerente();

        Long categoriaId = crearCategoria(
                "Categoría inventario",
                gerenteToken
        );

        Long productoId = crearProducto(
                "Producto inventario",
                categoriaId,
                gerenteToken
        );

        String empleadoToken = tokenEmpleado();

        mockMvc.perform(
                        post(
                                "/api/productos/{productoId}/movimientos",
                                productoId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + empleadoToken
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "tipoMovimiento": "ENTRADA",
                                          "cantidad": 8
                                        }
                                        """
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidad").value(8))
                .andExpect(
                        jsonPath("$.tipoMovimiento")
                                .value("ENTRADA")
                );

        mockMvc.perform(
                        get(
                                "/api/productos/{productoId}/movimientos",
                                productoId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + empleadoToken
                                )
                )
                .andExpect(status().isOk());
    }

    @Test
    void clienteNoPuedeConsultarMovimientos()
            throws Exception {
        mockMvc.perform(
                        get("/api/productos/999/movimientos")
                                .header(
                                        "Authorization",
                                        "Bearer " + tokenCliente()
                                )
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void gerentePuedeCrearProductoYEliminarCategoriaVacia()
            throws Exception {
        String token = tokenGerente();

        Long categoriaProductoId = crearCategoria(
                "Categoría producto",
                token
        );

        crearProducto(
                "Producto gerente",
                categoriaProductoId,
                token
        );

        Long categoriaVaciaId = crearCategoria(
                "Categoría vacía",
                token
        );

        mockMvc.perform(
                        delete(
                                "/api/categorias/{id}",
                                categoriaVaciaId
                        )
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());
    }

    @Test
    void openApiPublicaEsquemaBearerAuth()
            throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath(
                                "$.components.securitySchemes"
                                        + ".bearerAuth.type"
                        ).value("http")
                )
                .andExpect(
                        jsonPath(
                                "$.components.securitySchemes"
                                        + ".bearerAuth.scheme"
                        ).value("bearer")
                )
                .andExpect(
                        jsonPath(
                                "$.components.securitySchemes"
                                        + ".bearerAuth.bearerFormat"
                        ).value("JWT")
                )
                .andExpect(
                        jsonPath(
                                "$.security[0].bearerAuth"
                        ).exists()
                );
    }

    private Long crearCategoria(
            String nombre,
            String token
    ) throws Exception {
        String response = mockMvc.perform(
                        post("/api/categorias")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "nombre": "%s"
                                        }
                                        """.formatted(nombre)
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private Long crearProducto(
            String nombre,
            Long categoriaId,
            String token
    ) throws Exception {
        String response = mockMvc.perform(
                        post("/api/productos")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        """
                                        {
                                          "nombre": "%s",
                                          "precio": 1990.00,
                                          "categoriaId": %d
                                        }
                                        """.formatted(
                                                nombre,
                                                categoriaId
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);
        return json.get("id").asLong();
    }

    private String tokenCliente() {
        return generarToken(
                "cliente",
                List.of("ROLE_CLIENTE"),
                JWT_ISSUER,
                Instant.now().plusSeconds(3600)
        );
    }

    private String tokenEmpleado() {
        return generarToken(
                "empleado",
                List.of("ROLE_EMPLEADO"),
                JWT_ISSUER,
                Instant.now().plusSeconds(3600)
        );
    }

    private String tokenGerente() {
        return generarToken(
                "gerente",
                List.of("ROLE_GERENTE"),
                JWT_ISSUER,
                Instant.now().plusSeconds(3600)
        );
    }

    private String generarToken(
            String username,
            List<String> roles,
            String issuer,
            Instant expiration
    ) {
        Instant issuedAt = expiration.isBefore(Instant.now())
                ? expiration.minusSeconds(3600)
                : Instant.now();

        return Jwts.builder()
                .subject(username)
                .issuer(issuer)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .claim("roles", roles)
                .signWith(secretKey)
                .compact();
    }
}
