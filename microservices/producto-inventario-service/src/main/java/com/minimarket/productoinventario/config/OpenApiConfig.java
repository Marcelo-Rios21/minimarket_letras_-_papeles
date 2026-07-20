package com.minimarket.productoinventario.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME =
            "bearerAuth";

    @Bean
    public OpenAPI productoInventarioOpenApi() {
        SecurityScheme bearerScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .name("Authorization")
                .in(SecurityScheme.In.HEADER)
                .description(
                        "Token JWT emitido por auth-service. "
                                + "Swagger agrega automáticamente "
                                + "el prefijo Bearer."
                );

        return new OpenAPI()
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        SECURITY_SCHEME_NAME,
                                        bearerScheme
                                )
                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(SECURITY_SCHEME_NAME)
                )
                .info(
                        new Info()
                                .title(
                                        "API de Productos e Inventario"
                                )
                                .version("1.0.0")
                                .description(
                                        "Microservicio encargado del "
                                                + "catálogo, categorías, "
                                                + "stock y movimientos "
                                                + "de inventario."
                                )
                                .contact(
                                        new Contact()
                                                .name(
                                                        "MiniMarket "
                                                                + "Letras "
                                                                + "& Papeles"
                                                )
                                )
                )
                .addTagsItem(
                        new Tag()
                                .name("Categorías")
                                .description(
                                        "Administración de categorías "
                                                + "de productos"
                                )
                )
                .addTagsItem(
                        new Tag()
                                .name("Productos")
                                .description(
                                        "Administración del catálogo "
                                                + "de productos"
                                )
                )
                .addTagsItem(
                        new Tag()
                                .name("Inventario")
                                .description(
                                        "Entradas, salidas e historial "
                                                + "de stock"
                                )
                );
    }
}

