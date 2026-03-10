package com.rpg.rpghxh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RPG HxH API")
                        .version("1.0.0")
                        .description("API para gerenciamento do sistema RPG Hunter x Hunter")
                        .contact(new Contact()
                                .name("Equipe de Desenvolvimento")
                                .email("paganotiarthur@gmail.com")));
    }
}
