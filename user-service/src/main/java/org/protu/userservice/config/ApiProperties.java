package org.protu.userservice.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Data @Validated
@ConfigurationProperties(prefix = "api")
public class ApiProperties {
  @NotBlank
  String version;
}
