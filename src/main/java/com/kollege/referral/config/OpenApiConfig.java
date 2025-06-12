package com.kollege.referral.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.ExternalDocumentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

        @Value("${server.port:8080}")
        private String serverPort;

        @Bean
        public OpenAPI referralSystemOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Kollege Referral System API")
                                                .description("""
                                                                API documentation for the Kollege Multi-Level Referral and Earning System.

                                                                ## Features
                                                                - User Registration and Management
                                                                - Referral Code Generation and Tracking
                                                                - Multi-Level Referral Tree
                                                                - Earnings Management
                                                                - Real-time Notifications
                                                                - Analytics and Reporting

                                                                ## Authentication
                                                                All secured endpoints require a JWT token to be passed in the Authorization header:
                                                                `Authorization: Bearer <your_jwt_token>`
                                                                """)
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Kollege Support")
                                                                .email("support@kollege.com")
                                                                .url("https://kollege.com/support"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .externalDocs(new ExternalDocumentation()
                                                .description("Kollege Referral System Documentation")
                                                .url("https://kollege.com/docs"))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:" + serverPort)
                                                                .description("Development Server"),
                                                new Server()
                                                                .url("https://api.kollege.com")
                                                                .description("Production Server")))
                                .components(new Components()
                                                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                                                .type(SecurityScheme.Type.HTTP)
                                                                .scheme("bearer")
                                                                .bearerFormat("JWT")
                                                                .description("""
                                                                                JWT Authentication:
                                                                                1. Obtain JWT token via login
                                                                                2. Add token to request header
                                                                                3. Format: Bearer <token>
                                                                                """)))
                                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
        }
}