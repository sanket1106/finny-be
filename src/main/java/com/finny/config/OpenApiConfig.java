package com.finny.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        private static final String BEARER_SCHEME = "bearerAuth";

        @Bean
        public OpenAPI finnyOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Finny API")
                                                .description("Multi-tenant expense manager REST API")
                                                .version("v1")
                                                .contact(new Contact().name("Finny Team")))
                                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                                .components(new Components()
                                                .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                                                .name(BEARER_SCHEME)
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("UUID")
                                                                .description("Paste the token returned by POST /api/v1/auth/login")));
        }
}
