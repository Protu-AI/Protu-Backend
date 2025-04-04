package org.protu.contentservice.common.helpers;

import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtHelper {
  private final JwtDecoder jwtDecoder;

  private <T> T extractClaim(String token, String claim) {
    Jwt jwt = jwtDecoder.decode(token);
    return jwt.getClaim(claim);
  }

  public String extractUserRoles(String token) {
    return extractClaim(token, "roles");
  }

  public Long extractUserId(String token) {
    return extractClaim(token, "id");
  }
}
