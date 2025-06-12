package com.kollege.referral.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI referralSystemOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Kollege Referral System API")
                                                .description("API documentation for the multi-level referral and earning system")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Kollege Support")
                                                                .email("support@kollege.com")
                                                                .url("https://kollege.com/support"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("JWT token authentication")))
                                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        }
}