package org.protu.userservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDetailsForAdminDto(String publicId, String username, String firstName, String lastName, String email,
                                     String phoneNumber, String imageUrl, List<String> roles,
                                     boolean isEmailVerified, boolean isActive,
                                     Timestamp createdAt, Timestamp updatedAt) {
}
