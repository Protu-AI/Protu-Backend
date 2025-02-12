package org.protu.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppPropertiesConfig;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTService {
  private final RedisTemplate<Object, Object> redisTemplate;
  private final AppPropertiesConfig appPropertiesConfig;

  private String generateToken(String userId, long expiryTime) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(userId)
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plus(expiryTime, ChronoUnit.MILLIS)))
        .signWith(getSigningKey())
        .compact();
  }

  public String generateAccessToken(String userId) {
    return generateToken(userId, appPropertiesConfig.getJwt().getAccessTokenTtL());
  }

  public String generateRefreshToken(String userId) {
    return generateToken(userId, appPropertiesConfig.getJwt().getRefreshTokenTtL());
  }

  public String getAccessTokenDuration() {
    return appPropertiesConfig.getJwt().getAccessTokenTtL() / (1000 * 60) + " minutes";
  }

  public String getRefreshTokenDuration() {
    return appPropertiesConfig.getJwt().getRefreshTokenTtL() / (1000 * 60) + " minutes";
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(getSigningKey())
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(appPropertiesConfig.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
  }

  public String getTokenFromHeader(String authHeader) {
    return authHeader.split(" ")[1];
  }

  public Long getRefreshTokenDurationInMinutes() {
    return appPropertiesConfig.getJwt().getRefreshTokenTtL() / (1000 * 60);
  }

  public String getUserIdFromToken(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(Date.from(Instant.now()));
  }

  private boolean isNotBlackListedToken(String token) {
    Date lastInvalidDate = (Date) redisTemplate.opsForValue().get(appPropertiesConfig.getOtp().getPrefix().getJwt()+ getUserIdFromToken(token));
    Date issuedDate = extractClaim(token, Claims::getIssuedAt);
    return lastInvalidDate == null || issuedDate.after(lastInvalidDate);
  }

  public boolean isValidToken(String token, String userId) {
    return !isTokenExpired(token) && getUserIdFromToken(token).equals(userId) && isNotBlackListedToken(token);
  }

  public void invalidateUserTokens(Long id){
    Date currentDate = Date.from(Instant.now());
    Duration durationInSeconds =  Duration.ofMinutes(getRefreshTokenDurationInMinutes());
    redisTemplate.opsForValue().set(id, currentDate, durationInSeconds);
  }
}
