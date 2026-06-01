package com.mavic.backend.config;

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
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Restaurant Order System API")
                        .version("1.0.0")
                        .description("""
                                # Restaurant Order System API Documentation
                                
                                A comprehensive RESTful API for managing restaurant orders, menus, and deliveries.
                                
                                ## Features
                                - 🔐 JWT-based authentication and authorization
                                - 🍽️ Restaurant and menu management
                                - 📦 Order placement and tracking
                                - 👨‍🍳 Kitchen order management
                                - 🚚 Delivery tracking
                                - 👤 Customer profile management
                                
                                ## Authentication
                                Most endpoints require authentication. To use protected endpoints:
                                1. Register a new user via `/api/auth/register`
                                2. Login via `/api/auth/login` to get a JWT token
                                3. Click the 'Authorize' button and enter: `Bearer <your-token>`
                                4. All subsequent requests will include the token
                                
                                ## User Roles
                                - **CUSTOMER**: Browse restaurants, place orders, track deliveries
                                - **RESTAURANT_ADMIN**: Manage restaurant menu items
                                - **KITCHEN_STAFF**: Accept/reject orders, mark orders ready
                                - **DELIVERY_DRIVER**: Pick up and deliver orders
                                
                                ## Rate Limiting
                                - Auth endpoints: 5 requests per minute
                                - Other endpoints: 20 requests per minute
                                
                                ## Order Status Flow
                                ```
                                PENDING → ACCEPTED → PREPARING → READY → PICKED_UP → DELIVERED
                                        ↘ REJECTED
                                        ↘ CANCELLED (only from PENDING)
                                ```
                                """)
                        .contact(new Contact()
                                .name("Mavic Backend Team")
                                .email("support@mavic.com")
                                .url("https://mavic.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.mavic.com")
                                .description("Production Server")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token obtained from login endpoint")))
                .tags(Arrays.asList(
                        new Tag()
                                .name("Authentication")
                                .description("User registration and login endpoints"),
                        new Tag()
                                .name("Restaurants")
                                .description("Browse restaurants and menus (public access)"),
                        new Tag()
                                .name("Menu Management")
                                .description("Restaurant admin operations for managing menu items"),
                        new Tag()
                                .name("Customer Orders")
                                .description("Customer operations for placing and tracking orders"),
                        new Tag()
                                .name("Customer Profile")
                                .description("Customer profile management"),
                        new Tag()
                                .name("Kitchen Operations")
                                .description("Kitchen staff operations for managing orders"),
                        new Tag()
                                .name("Delivery Operations")
                                .description("Delivery driver operations for order fulfillment")
                ));
    }
}
