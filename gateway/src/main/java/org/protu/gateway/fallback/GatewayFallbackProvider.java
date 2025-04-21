package org.protu.gateway.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class GatewayFallbackProvider implements HandlerFunction<ServerResponse> {

  @Override
  public Mono<ServerResponse> handle(ServerRequest request) {
    Map<String, Object> response = new HashMap<>();
    response.put("error", "Service Unavailable");
    response.put("message", "The requested service is currently unavailable. Please try again later.");

    return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
        .contentType(MediaType.APPLICATION_JSON)
        .body(BodyInserters.fromValue(response));
  }
}