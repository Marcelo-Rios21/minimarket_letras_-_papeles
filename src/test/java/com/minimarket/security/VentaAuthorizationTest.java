package com.minimarket.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.entity.Venta;
import com.minimarket.service.VentaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VentaAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VentaService ventaService;

    @BeforeEach
    void configurarMocks() {
        Venta ventaRegistrada = new Venta();
        ventaRegistrada.setId(1L);
        ventaRegistrada.setFecha(new Date());
        ventaRegistrada.setTotal(2400.0);

        when(ventaService.registrarVenta(ArgumentMatchers.any(Venta.class)))
                .thenReturn(ventaRegistrada);
    }

    @Test
    void generarVentaSinTokenDebeRetornarNoAutorizado() throws Exception {
        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaJson()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoDebeGenerarVenta() throws Exception {
        String tokenCliente = obtenerToken("cliente", "cliente123");

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + tokenCliente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorNoDebeGenerarVentaSegunPautaEstricta() throws Exception {
        String tokenGerente = obtenerToken("gerente", "gerente123");

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + tokenGerente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void cajeroDebeGenerarVenta() throws Exception {
        String tokenEmpleado = obtenerToken("empleado", "empleado123");

        mockMvc.perform(post("/api/ventas")
                        .header("Authorization", "Bearer " + tokenEmpleado)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ventaJson()))
                .andExpect(status().isCreated());
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

    private String ventaJson() {
        return """
                {
                    "usuario": {
                        "id": 1
                    },
                    "detalles": [
                        {
                            "producto": {
                                "id": 1
                            },
                            "cantidad": 2
                        }
                    ]
                }
                """;
    }
}