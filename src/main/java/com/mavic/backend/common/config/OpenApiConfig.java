package com.mavic.backend.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Order System API")
                        .version("1.0.0")
                        .description("""
                                # Restaurant Order System API
                                
                                ## Authentication
                                1. Register via `POST /api/auth/register/customer`
                                2. Login via `POST /api/auth/login` to get a JWT token
                                3. Click **Authorize** and enter: `Bearer <your-token>`
                                
                                ## User Roles
                                - **CUSTOMER** — Browse restaurants, place orders, manage profile
                                - **KITCHEN_STAFF** — Accept/reject orders, mark orders ready
                                - **DELIVERY_DRIVER** — Pick up and deliver orders
                                - **RESTAURANT_ADMIN** — Manage customers, menu, staff, and admin invitations
                                
                                ## Order Status Flow
                                ```
                                PENDING → ACCEPTED → PREPARING → READY → PICKED_UP → DELIVERED
                                        ↘ REJECTED
                                        ↘ CANCELLED (only from PENDING)
                                ```
                                """)
                        .contact(new Contact()
                                .name("Mavic Backend Team")
                                .email("support@mavic.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080").description("Development Server"),
                        new Server().url("https://api.mavic.com").description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from login endpoint")))
                .tags(Arrays.asList(
                        new Tag().name("1. Authentication")
                                .description("Registration and login"),
                        new Tag().name("2. Customer")
                                .description("Customer profiles and order operations"),
                        new Tag().name("3. Kitchen Staff")
                                .description("Kitchen order management and preparation workflow"),
                        new Tag().name("4. Delivery Driver")
                                .description("Order pickup and delivery tracking"),
                        new Tag().name("5. Admin")
                                .description("Customer management, menu management, staff registration, and admin invitations"),
                        new Tag().name("6. Public - Restaurants")
                                .description("Browse restaurants and menus (public access)")
                ));
    }
}
