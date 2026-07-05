package com.minimarket.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI minimarketOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Minimarket Plus API")
                        .version("1.0.0")
                        .description("Documentacion tecnica de los servicios REST del backend de Minimarket Plus."));
    }
}