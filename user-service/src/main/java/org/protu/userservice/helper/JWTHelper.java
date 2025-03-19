package org.protu.userservice.helper;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JWTHelper {
  private final AppProperties properties;

  public String generateToken(String userId, long expiryTime, String userRoles) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId)
        .claim("roles", userRoles)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expiryTime, ChronoUnit.MILLIS)))
        .signWith(getSigningKey())
        .compact();
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  public Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public String extractUserRoles(String token) {
    return extractClaim(token, claims -> claims.get("roles", String.class));
  }

  public SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(properties.jwt().secret().getBytes(StandardCharsets.UTF_8));
  }
}
