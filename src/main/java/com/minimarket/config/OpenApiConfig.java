package com.minimarket.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI minimarketOpenAPI() {
        SecurityScheme jwtSecurityScheme = new SecurityScheme()
                .name(SECURITY_SCHEME_NAME)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description(
                        "Ingrese el token JWT obtenido desde POST /api/auth/login. "
                                + "Swagger agregará automáticamente el prefijo Bearer."
                );

        return new OpenAPI()
                .components(
                        new Components().addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                jwtSecurityScheme
                        )
                )
                .info(
                        new Info()
                                .title("Minimarket Plus API")
                                .version("1.0.0")
                                .description(
                                        "Documentación técnica de los servicios REST "
                                                + "del backend de Minimarket Plus. "
                                                + "La API gestiona productos, categorías, "
                                                + "carritos, inventario, ventas y usuarios, "
                                                + "manteniendo autenticación JWT y autorización "
                                                + "basada en roles."
                                )
                );
    }
}