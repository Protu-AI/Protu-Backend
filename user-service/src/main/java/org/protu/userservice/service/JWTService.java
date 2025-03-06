package org.protu.userservice.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppPropertiesConfig;
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
  private final AppPropertiesConfig properties;
  private final JWTHelper jwtHelper;

  public String generateAccessToken(String userId) {
    return jwtHelper.generateToken(userId, properties.getJwt().getAccessTokenTtL());
  }

  public String generateRefreshToken(String userId) {
    return jwtHelper.generateToken(userId, properties.getJwt().getRefreshTokenTtL());
  }

  public String getAccessTokenDuration() {
    return properties.getJwt().getAccessTokenTtL() / (1000 * 60) + " minutes";
  }

  public String getRefreshTokenDuration() {
    return properties.getJwt().getRefreshTokenTtL() / (1000 * 60) + " minutes";
  }

  public String getTokenFromHeader(String authHeader) {
    return authHeader.split(" ")[1];
  }

  public Long getRefreshTokenDurationInMinutes() {
    return properties.getJwt().getRefreshTokenTtL() / (1000 * 60);
  }

  public String getUserIdFromToken(String token) {
    return jwtHelper.extractClaim(token, Claims::getSubject);
  }

  private boolean isTokenExpired(String token) {
    return jwtHelper.extractExpiration(token).before(Date.from(Instant.now()));
  }

  private boolean isNotBlackListedToken(String token) {
    Date issuedDate = jwtHelper.extractClaim(token, Claims::getIssuedAt);
    String publicId = getUserIdFromToken(token);
    String dateString = redisTemplate.opsForValue().get(properties.getOtp().getPrefix().getJwt() + publicId);
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
    redisTemplate.opsForValue().set(properties.getOtp().getPrefix().getJwt() + publicId, String.valueOf(currentDate), durationInMillis);
  }
}
