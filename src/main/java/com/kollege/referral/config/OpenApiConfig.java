package com.kollege.referral.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

        @Bean
        public OpenAPI referralSystemOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Referral System API")
                                                .description("Multi-Level Referral & Earning System REST API documentation")
                                                .version("1.0")
                                                .contact(new Contact()
                                                                .name("Kollege")
                                                                .email("support@kollege.com"))
                                                .license(new License()
                                                                .name("Apache 2.0")
                                                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")));
        }
}