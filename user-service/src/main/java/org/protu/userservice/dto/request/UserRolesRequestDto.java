package org.protu.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRolesRequestDto(
    @NotBlank(message = "role is required") @Size(max = 50, message = "Role must not exceed 50 characters")
    String role) {
}
