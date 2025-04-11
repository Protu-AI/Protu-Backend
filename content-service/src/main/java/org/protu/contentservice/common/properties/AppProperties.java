package org.protu.contentservice.common.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app")
public record AppProperties(Api api, JWT jwt, Rabbit rabbit) {

  public record Api(@NotBlank String version) {
  }

  public record JWT(@NotBlank String secret) {
  }

  public record Rabbit(Exchange exchange, Queue queue, RoutingKey routingKey) {

    public record Exchange(@NotBlank String userEvents) {
    }

    public record Queue(@NotBlank String userReplica) {
    }

    public record RoutingKey(
        @NotBlank String userCreated,
        @NotBlank String userUpdated,
        @NotBlank String userDeleted,
        @NotBlank String userPattern) {
    }
  }
}
