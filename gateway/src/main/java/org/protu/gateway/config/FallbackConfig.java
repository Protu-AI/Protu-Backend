package org.protu.gateway.config;

import org.protu.gateway.fallback.GatewayFallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class FallbackConfig {

  @Bean
  public RouterFunction<ServerResponse> routerFunction(GatewayFallbackProvider fallbackProvider) {
    return RouterFunctions.route(RequestPredicates.path("/fallback/user-service"), fallbackProvider)
        .andRoute(RequestPredicates.path("/fallback/chat-service"), fallbackProvider)
        .andRoute(RequestPredicates.path("/fallback/code-execution-service"), fallbackProvider)
        .andRoute(RequestPredicates.path("/fallback/content-service"), fallbackProvider);
  }
}