package br.com.fiap.leitesolidario.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info().title("Leite Solidário API").version("1.0.0")
                .description("Checkpoint 2 — Spring Boot e SQL Server. João Castro — RM 554628."));
    }
}
