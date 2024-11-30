package org.protu.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
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
  private long accessTokenExpiryTime;

  @Value("${jwt.refresh-token-expiration-time}")
  private long refreshTokenExpiryTime;

  private String generateToken(String username, long expiryTime) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(username)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expiryTime, ChronoUnit.MILLIS)))
        .signWith(getSigningKey())
        .compact();
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(Date.from(Instant.now()));
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
  public String getUsernameFromToken(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  @Override
  public String generateAccessToken(String username) {
    return generateToken(username, accessTokenExpiryTime);
  }

  @Override
  public String generateRefreshToken(String username) {
    return generateToken(username, refreshTokenExpiryTime);
  }

  @Override
  public boolean validateToken(String token, String username) {
    final String usernameFromToken = getUsernameFromToken(token);
    return (usernameFromToken.equals(username) && !isTokenExpired(token));
  }
}
