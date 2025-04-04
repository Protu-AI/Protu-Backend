package org.protu.userservice.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(Api api, JWT jwt, Otp otp, Cloudinary cloudinary, RabbitMQ rabbitMQ) {

  public record Api(@NotBlank String version) {
  }

  public record JWT(
      @NotBlank String secret,
      long accessTokenTtL,
      long refreshTokenTtL) {
  }

  public record Otp(
      long emailTtl,
      long passwordTtl,
      Prefix prefix) {

    public record Prefix(
        @NotBlank String jwt,
        @NotBlank String email,
        @NotBlank String password) {
    }
  }

  public record Cloudinary(
      @NotBlank String cloudName,
      @NotBlank String apiKey,
      @NotBlank String apiSecret) {
  }

  public record RabbitMQ(
      @NotBlank String emailMainQueue,
      @NotBlank String emailRetryQueue
  ) {

  }
}
