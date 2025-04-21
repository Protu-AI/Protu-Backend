package org.protu.notificationservice.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.rabbit")
public record AppProperties(Queue queue, Retry retry) {

  public record Queue(Email email) {
    public record Email(@NotBlank String main, @NotBlank String retry, @NotBlank String dead) {
    }
  }

  public record Retry(
      int count,
      long delay) {
  }
}
