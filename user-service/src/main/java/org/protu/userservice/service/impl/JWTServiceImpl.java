package org.protu.userservice.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.protu.userservice.service.JWTService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Service
public class JWTServiceImpl implements JWTService {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.access-token-expiration-time}")
  private String accessTokenExpiryTime;

  @Value("${jwt.refresh-token-expiration-time}")
  private String refreshTokenExpiryTime;

  private String generateToken(Long userId, long expiryTime) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expiryTime, ChronoUnit.MILLIS)))
        .signWith(getSigningKey())
        .compact();
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  @Override
  public String getTokenFromHeader(String authHeader) {
    return authHeader.split(" ")[1];
  }

  @Override
  public String getAccessTokenDuration() {
    return Long.parseLong(accessTokenExpiryTime) / (1000 * 60) + " minutes";
  }

  @Override
  public String getRefreshTokenDuration() {
    return Long.parseLong(refreshTokenExpiryTime) / (1000 * 60) + " minutes";
  }

  @Override
  public boolean isTokenExpired(String token) {
    return extractExpiration(token).before(Date.from(Instant.now()));
  }

  @Override
  public Long getUserIdFromToken(String token) {
    return Long.parseLong(extractClaim(token, Claims::getSubject));
  }

  @Override
  public String generateAccessToken(Long userId) {
    return generateToken(userId, Long.parseLong(accessTokenExpiryTime));
  }

  @Override
  public String generateRefreshToken(Long userId) {
    return generateToken(userId, Long.parseLong(refreshTokenExpiryTime));
  }

  @Override
  public boolean isValidToken(String token, Long userId) {
    return !isTokenExpired(token) && getUserIdFromToken(token).equals(userId);
  }
}
