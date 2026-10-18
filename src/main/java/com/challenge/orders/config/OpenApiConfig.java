package com.challenge.orders.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderManagementOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Order Management API")
                .version("1.0.0")
                .description("""
                        API REST para gestion de ordenes de compra.

                        Maquina de estados: CREADA -> PAGADA -> ENVIADA -> ENTREGADA; \
                        CANCELADA es alcanzable desde CREADA, PAGADA y ENVIADA.

                        Errores en formato RFC 7807 con campo `code` (RN-01..RN-06, VALIDATION_ERROR, ORDER_NOT_FOUND...).""")
                .contact(new Contact().name("QA Engineering")));
    }
}
