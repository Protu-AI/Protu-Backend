package org.protu.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationRequestDTO {
  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  String email;

  @NotBlank(message = "Verification code is required")
  String verificationCode;
}
