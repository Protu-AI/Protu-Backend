package main.java.org.protu.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service-route", r -> r
                        .path("/api/v1/auth/**", "/api/v1/users/**")
                        .uri("lb://USER-SERVICE"))
                
                // Chat Service Routes
                .route("chat-service-route", r -> r
                        .path("/api/v1/messages/**", "/api/v1/chats/**", "/api/v1/attachments/**")
                        .uri("lb://CHAT-SERVICE"))
                
                // Code Execution Service Routes
                .route("code-execution-service-route", r -> r
                        .path("/api/v1/execute/**")
                        .uri("lb://CODE-EXECUTION-SERVICE"))
                
                // Health Check Route
                .route("health-route", r -> r
                        .path("/health")
                        .filters(f -> f.setPath("/actuator/health"))
                        .uri("lb://GATEWAY"))
                
                .build();
    }
}