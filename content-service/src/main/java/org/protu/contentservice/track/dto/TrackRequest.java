package org.protu.contentservice.track.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrackRequest(
    @NotBlank(message = "Track name is required") @Size(max = 30, message = "Track name must not exceed 30 characters") String name,
    @NotBlank(message = "description is required") String description) {
}
