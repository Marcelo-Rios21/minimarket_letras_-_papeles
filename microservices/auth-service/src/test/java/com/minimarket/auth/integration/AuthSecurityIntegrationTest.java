package com.minimarket.auth.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.minimarket.auth.entity.NombreRol;
import com.minimarket.auth.entity.Rol;
import com.minimarket.auth.entity.Usuario;
import com.minimarket.auth.repository.RolRepository;
import com.minimarket.auth.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.minimarket.auth.entity.NombreRol.ROLE_CLIENTE;
import static com.minimarket.auth.entity.NombreRol.ROLE_EMPLEADO;
import static com.minimarket.auth.entity.NombreRol.ROLE_GERENTE;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthSecurityIntegrationTest {

    private static final String GERENTE_USERNAME = "gerente";
    private static final String GERENTE_PASSWORD = "Gerente123";

    private static final String CLIENTE_USERNAME = "cliente";
    private static final String CLIENTE_PASSWORD = "Cliente123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long clienteId;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        crearUsuario(
                GERENTE_USERNAME,
                GERENTE_PASSWORD,
                Set.of(ROLE_GERENTE)
        );

        Usuario cliente = crearUsuario(
                CLIENTE_USERNAME,
                CLIENTE_PASSWORD,
                Set.of(ROLE_CLIENTE)
        );

        clienteId = cliente.getId();
    }

    @Test
    void registrarClienteDevuelve201YRoleCliente()
            throws Exception {
        Map<String, Object> request = Map.of(
                "username", "nuevo.cliente",
                "password", "Password123",
                "nombre", "Nuevo",
                "apellido", "Cliente",
                "email", "nuevo@correo.cl",
                "direccion", "Calle Nueva 123"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        header().string(
                                "Location",
                                org.hamcrest.Matchers.matchesPattern(
                                        "/api/usuarios/\\d+"
                                )
                        )
                )
                .andExpect(
                        jsonPath("$.username")
                                .value("nuevo.cliente")
                )
                .andExpect(
                        jsonPath("$.activo")
                                .value(true)
                )
                .andExpect(
                        jsonPath("$.roles[0]")
                                .value("ROLE_CLIENTE")
                )
                .andExpect(
                        jsonPath("$.password")
                                .doesNotExist()
                );
    }

    @Test
    void registroInvalidoDevuelve400ConErroresDeCampo()
            throws Exception {
        Map<String, Object> request = Map.of(
                "username", "x",
                "password", "123",
                "nombre", "",
                "apellido", "",
                "email", "correo-invalido",
                "direccion", ""
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "La solicitud contiene campos inválidos"
                                )
                )
                .andExpect(
                        jsonPath("$.fieldErrors")
                                .isArray()
                )
                .andExpect(
                        jsonPath("$.fieldErrors")
                                .isNotEmpty()
                );
    }

    @Test
    void registroDuplicadoDevuelve409()
            throws Exception {
        Map<String, Object> request = Map.of(
                "username", "CLIENTE",
                "password", "Password123",
                "nombre", "Otro",
                "apellido", "Cliente",
                "email", "otro@correo.cl",
                "direccion", "Calle Duplicada"
        );

        mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "El username ya se encuentra registrado"
                                )
                );
    }

    @Test
    void loginDevuelveJwtYDatosDelUsuario()
            throws Exception {
        Map<String, Object> request = Map.of(
                "username", " GeReNtE ",
                "password", GERENTE_PASSWORD
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.token")
                                .isNotEmpty()
                )
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expiresInSeconds")
                                .value(3600)
                )
                .andExpect(
                        jsonPath("$.username")
                                .value(GERENTE_USERNAME)
                )
                .andExpect(
                        jsonPath("$.roles[0]")
                                .value("ROLE_GERENTE")
                );
    }

    @Test
    void loginConPasswordIncorrectaDevuelve401()
            throws Exception {
        Map<String, Object> request = Map.of(
                "username", GERENTE_USERNAME,
                "password", "Incorrecta123"
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("Credenciales inválidas")
                );
    }

    @Test
    void listarUsuariosSinTokenDevuelve401()
            throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Se requiere autenticación "
                                                + "para acceder al recurso"
                                )
                );
    }

    @Test
    void clienteNoPuedeListarUsuarios()
            throws Exception {
        String token = obtenerToken(
                CLIENTE_USERNAME,
                CLIENTE_PASSWORD
        );

        mockMvc.perform(
                        get("/api/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isForbidden())
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "No posee permisos "
                                                + "para acceder al recurso"
                                )
                );
    }

    @Test
    void gerentePuedeListarUsuariosConHateoas()
            throws Exception {
        String token = obtenerToken(
                GERENTE_USERNAME,
                GERENTE_PASSWORD
        );

        mockMvc.perform(
                        get("/api/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$._links.self.href")
                                .value(
                                        endsWith("/api/usuarios")
                                )
                )
                .andExpect(
                        jsonPath("$._embedded")
                                .exists()
                );
    }

    @Test
    void gerentePuedeCrearUsuarioConRoles()
            throws Exception {
        String token = obtenerToken(
                GERENTE_USERNAME,
                GERENTE_PASSWORD
        );

        Map<String, Object> request = Map.of(
                "username", "empleado.nuevo",
                "password", "Empleado123",
                "nombre", "Ana",
                "apellido", "Pérez",
                "email", "ana@correo.cl",
                "direccion", "Sucursal Central",
                "roles", Set.of(
                        "ROLE_EMPLEADO",
                        "ROLE_CLIENTE"
                )
        );

        mockMvc.perform(
                        post("/api/usuarios")
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.username")
                                .value("empleado.nuevo")
                )
                .andExpect(
                        jsonPath("$.roles")
                                .value(
                                        containsInAnyOrder(
                                                "ROLE_CLIENTE",
                                                "ROLE_EMPLEADO"
                                        )
                                )
                )
                .andExpect(
                        jsonPath("$._links.self.href")
                                .exists()
                )
                .andExpect(
                        jsonPath("$._links.collection.href")
                                .exists()
                );
    }

    @Test
    void desactivarUsuarioImpideLoginPosterior()
            throws Exception {
        String token = obtenerToken(
                GERENTE_USERNAME,
                GERENTE_PASSWORD
        );

        mockMvc.perform(
                        delete("/api/usuarios/{id}", clienteId)
                                .header(
                                        "Authorization",
                                        "Bearer " + token
                                )
                )
                .andExpect(status().isNoContent());

        Map<String, Object> loginRequest = Map.of(
                "username", CLIENTE_USERNAME,
                "password", CLIENTE_PASSWORD
        );

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                loginRequest
                                        )
                                )
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.message")
                                .value("Credenciales inválidas")
                );
    }

    private Usuario crearUsuario(
            String username,
            String password,
            Set<NombreRol> nombresRoles
    ) {
        Set<Rol> roles = nombresRoles
                .stream()
                .map(nombre -> rolRepository
                        .findByNombre(nombre)
                        .orElseThrow()
                )
                .collect(Collectors.toSet());

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPassword(
                passwordEncoder.encode(password)
        );
        usuario.setNombre("Usuario");
        usuario.setApellido(username);
        usuario.setEmail(username + "@correo.cl");
        usuario.setDireccion("Dirección de prueba");
        usuario.setRoles(roles);
        usuario.setActivo(true);

        return usuarioRepository.saveAndFlush(usuario);
    }

    private String obtenerToken(
            String username,
            String password
    ) throws Exception {
        Map<String, Object> request = Map.of(
                "username", username,
                "password", password
        );

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("token").asText();
    }
}
