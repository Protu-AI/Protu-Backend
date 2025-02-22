package org.protu.userservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordReqDto(
    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters long")
    String oldPassword,

    @NotBlank @Size(min = 8, message = "Password must be at least 8 characters long")
    String newPassword) {}
