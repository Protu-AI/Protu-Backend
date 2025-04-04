package org.protu.contentservice.common.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("app")
public record AppProperties(Api api, JWT jwt) {

  public record Api(@NotBlank String version) {
  }

  public record JWT(@NotBlank String secret) {
  }
}
