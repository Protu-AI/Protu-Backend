package org.protu.userservice.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TokensResDto {
  Long userId;
  String accessToken;
  String refreshToken;
  String accessTokenExpiresIn;
  String refreshTokenExpiresIn;
  String tokenType;
}
