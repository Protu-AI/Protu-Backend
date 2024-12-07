package org.protu.userservice.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokensResponseDto {
  Long userId;
  String accessToken;
  String refreshToken;
  String accessTokenExpiresIn;
  String refreshTokenExpiresIn;
  String tokenType;
}
