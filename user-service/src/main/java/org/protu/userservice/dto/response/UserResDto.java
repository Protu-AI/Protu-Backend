package org.protu.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResDto(String publicId, String username, String firstName, String lastName, String email,
                         String phoneNumber, String imageUrl) {
}
