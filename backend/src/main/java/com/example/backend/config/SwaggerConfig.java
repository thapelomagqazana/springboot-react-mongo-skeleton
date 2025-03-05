package com.example.backend.config;

import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI backendApiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Backend API")
                        .description("Spring Boot API documentation with Swagger")
                        .version("1.0.0"));
    }
}
