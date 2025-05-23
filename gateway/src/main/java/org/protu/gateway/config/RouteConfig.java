package org.protu.gateway.config;

import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.List;

@Configuration
public class RouteConfig {

  @Autowired
  private DiscoveryClient discoveryClient;
  
  @Autowired
  private Environment env;

  @Bean
  public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    // Check if Eureka is available and services are registered
    List<String> services = discoveryClient.getServices();
    boolean userServiceAvailable = services.contains("USER-SERVICE");
    boolean chatServiceAvailable = services.contains("CHAT-SERVICE");
    boolean codeExecutionServiceAvailable = services.contains("CODE-EXECUTION-SERVICE");
    boolean contentServiceAvailable = services.contains("CONTENT-SERVICE");
    boolean quizServiceAvailable = services.contains("QUIZ-SERVICE");
    
    return builder.routes()
        // User Service Routes - use dynamic routing only if service is available
        .route("user-service-route", r -> r
            .path("/api/v1/auth/**", "/api/v1/users/**")
            .uri(userServiceAvailable ? "lb://USER-SERVICE" : 
                 env.getProperty("spring.cloud.gateway.routes[0].uri")))

        // Chat Service Routes
        .route("chat-service-route", r -> r
            .path("/api/v1/messages/**", "/api/v1/chats/**", "/api/v1/attachments/**")
            .uri(chatServiceAvailable ? "lb://CHAT-SERVICE" : 
                 env.getProperty("spring.cloud.gateway.routes[1].uri")))

        // Code Execution Service Routes
        .route("code-execution-service-route", r -> r
            .path("/api/v1/execute/**")
            .uri(codeExecutionServiceAvailable ? "lb://CODE-EXECUTION-SERVICE" : 
                 env.getProperty("spring.cloud.gateway.routes[2].uri")))
                 
        // Content Service Routes
        .route("content-service-route", r -> r
            .path("/api/v1/tracks/**", "/api/v1/lessons/**", "/api/v1/courses/**", "/api/v1/progress/**")
            .uri(contentServiceAvailable ? "lb://CONTENT-SERVICE" : 
                 env.getProperty("spring.cloud.gateway.routes[3].uri")))

        // Quiz Service Routes
        .route("quiz-service-route", r -> r
            .path("/api/v1/quizzes/**", "/api/v1/attempts/**")
            .uri(quizServiceAvailable ? "lb://QUIZ-SERVICE" : 
                 env.getProperty("spring.cloud.gateway.routes[4].uri")))

        // Health Check Route
        .route("health-route", r -> r
            .path("/health")
            .filters(f -> f.setPath("/actuator/health"))
            .uri("lb://GATEWAY"))

        .build();
  }
}