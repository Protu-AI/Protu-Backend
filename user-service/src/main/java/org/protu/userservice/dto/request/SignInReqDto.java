package org.protu.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInReqDto (
  @NotBlank(message = "userIdentifier is required")
  @Size(max = 100, message = "userIdentifier must be at least 8 characters long")
  String userIdentifier,

  @NotBlank
  @Size(min = 8, message = "Password must be at least 8 characters long")
  String password
){}
