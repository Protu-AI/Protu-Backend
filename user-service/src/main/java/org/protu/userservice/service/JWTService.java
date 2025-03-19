package org.protu.userservice.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.helper.JWTHelper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class JWTService {
  private final RedisTemplate<Object, String> redisTemplate;
  private final AppProperties properties;
  private final JWTHelper jwtHelper;

  public String generateAccessToken(String userId, String userRoles) {
    return jwtHelper.generateToken(userId, properties.jwt().accessTokenTtL(), userRoles);
  }

  public String generateRefreshToken(String userId, String userRoles) {
    return jwtHelper.generateToken(userId, properties.jwt().refreshTokenTtL(), userRoles);
  }

  public String getAccessTokenDuration() {
    return properties.jwt().accessTokenTtL() / (1000 * 60) + " minutes";
  }

  public String getRefreshTokenDuration() {
    return properties.jwt().refreshTokenTtL() / (1000 * 60) + " minutes";
  }

  public String getTokenFromHeader(String authHeader) {
    return authHeader.split(" ")[1];
  }

  public Long getRefreshTokenDurationInMinutes() {
    return properties.jwt().refreshTokenTtL() / (1000 * 60);
  }

  public String getUserIdFromToken(String token) {
    return jwtHelper.extractClaim(token, Claims::getSubject);
  }

  public String getUserRoles(String token) {
    return jwtHelper.extractUserRoles(token);
  }

  private boolean isTokenExpired(String token) {
    return jwtHelper.extractExpiration(token).before(Date.from(Instant.now()));
  }

  private boolean isNotBlackListedToken(String token) {
    Date issuedDate = jwtHelper.extractClaim(token, Claims::getIssuedAt);
    String publicId = getUserIdFromToken(token);
    String dateString = redisTemplate.opsForValue().get(properties.otp().prefix().jwt() + publicId);
    if (dateString == null)
      return true;

    long lastInvalidDate = Long.parseLong(dateString);
    return issuedDate.getTime() > lastInvalidDate;
  }

  public boolean isValidToken(String token, String userId) {
    return !isTokenExpired(token) && getUserIdFromToken(token).equals(userId) && isNotBlackListedToken(token);
  }

  public void blockCurrentUserTokens(String publicId) {
    long currentDate = Date.from(Instant.now()).getTime();
    Duration durationInMillis = Duration.ofMillis(getRefreshTokenDurationInMinutes() * 60 * 1000);
    redisTemplate.opsForValue().set(properties.otp().prefix().jwt() + publicId, String.valueOf(currentDate), durationInMillis);
  }
}
