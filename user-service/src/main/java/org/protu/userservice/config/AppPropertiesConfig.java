package org.protu.userservice.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Data @Validated
@ConfigurationProperties(prefix = "app")
public class AppPropertiesConfig {
  JWT jwt;
  Otp otp;

  @Data @Validated
  public static class JWT {
    @NotBlank
    String secret;

    @NotBlank
    long accessTokenTtL;

    @NotBlank
    long refreshTokenTtL;
  }

  @Data @Validated
  public static class Otp {
    @NotBlank
    long emailTtl;

    @NotBlank
    long passwordTtl;

    Prefix prefix;

    @Data @Validated
    public static class Prefix {
      @NotBlank
      String jwt;

      @NotBlank
      String email;

      @NotBlank
      String password;
    }
  }
}
