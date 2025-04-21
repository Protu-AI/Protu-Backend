package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.helper.JwtHelper;
import org.protu.userservice.model.User;
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
  private final JwtHelper jwtHelper;

  public String generateAccessToken(User user) {
    return jwtHelper.generateToken(user, properties.jwt().accessTokenTtL());
  }

  public String generateRefreshToken(User user) {
    return jwtHelper.generateToken(user, properties.jwt().refreshTokenTtL());
  }

  public String getAccessTokenDuration() {
    return properties.jwt().accessTokenTtL() / (1000 * 60) + " minutes";
  }

  public String getRefreshTokenDuration() {
    return properties.jwt().refreshTokenTtL() / (1000 * 60) + " minutes";
  }

  public String getTokenFromHeader(String authHeader) {
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Invalid Authorization header format");
    }

    return authHeader.split(" ")[1];
  }

  public Long getRefreshTokenDurationInMinutes() {
    return properties.jwt().refreshTokenTtL() / (1000 * 60);
  }

  public String getUserIdFromToken(String token) {
    return jwtHelper.extractUserId(token);
  }

  public String getUserRoles(String token) {
    return jwtHelper.extractUserRoles(token);
  }

  private boolean isNotBlackListedToken(String token) {
    Date issuedDate = jwtHelper.extractClaim(token, "iat");
    String publicId = getUserIdFromToken(token);
    String dateString = redisTemplate.opsForValue().get(properties.otp().prefix().jwt() + publicId);
    if (dateString == null)
      return true;

    long lastInvalidDate = Long.parseLong(dateString);
    return issuedDate.getTime() > lastInvalidDate;
  }

  public void blockCurrentUserTokens(String publicId) {
    long currentDate = Date.from(Instant.now()).getTime();
    Duration durationInMillis = Duration.ofMillis(getRefreshTokenDurationInMinutes() * 60 * 1000);
    redisTemplate.opsForValue().set(properties.otp().prefix().jwt() + publicId, String.valueOf(currentDate), durationInMillis);
  }
}
