package org.protu.userservice.dto.request;

import jakarta.validation.constraints.Size;
import org.protu.userservice.validation.NotEmptyIfPresent;

public record PartialUpdateReqDto (
  @NotEmptyIfPresent(message = "First name cannot be empty if provided")
  @Size(max = 50, message = "First name must not exceed 50 characters")
  String firstName,

  @NotEmptyIfPresent(message = "Last name cannot be empty if provided")
  @Size(max = 50, message = "Last name must not exceed 50 characters")
  String lastName,

  @NotEmptyIfPresent(message = "Username cannot be empty if provided")
  @Size(max = 50, message = "Username must not exceed 50 characters")
  String username,

  @NotEmptyIfPresent(message = "Phone number cannot be empty if provided")
  @Size(max = 20, message = "Phone number must not exceed 20 characters")
  String phoneNumber,

  @NotEmptyIfPresent(message = "Password cannot be empty if provided")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  String password
) {}
