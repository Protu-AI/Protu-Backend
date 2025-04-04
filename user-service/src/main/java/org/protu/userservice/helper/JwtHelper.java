package org.protu.userservice.helper;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.model.User;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtHelper {
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;

  public String generateToken(User user, long ttlInMillis) {
    JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS256).build();
    Instant now = Instant.now();
    JwtClaimsSet jwtClaims = JwtClaimsSet.builder()
        .issuer("user-service")
        .subject(user.getPublicId())
        .claim("id", user.getId())
        .claim("roles", user.getRoles())
        .issuedAt(now)
        .expiresAt(Date.from(now).toInstant().plusMillis(ttlInMillis))
        .build();
    return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaims)).getTokenValue();
  }

  public <T> T extractClaim(String token, String claim) {
    Jwt jwt = jwtDecoder.decode(token);
    return jwt.getClaim(claim);
  }

  public String extractUserRoles(String token) {
    return extractClaim(token, "roles");
  }

  public String extractUserId(String token) {
    return extractClaim(token, "sub");
  }
}
