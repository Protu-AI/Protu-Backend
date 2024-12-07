package org.protu.userservice.service;

public interface JWTService {

  String generateRefreshToken(Long userId);

  String generateAccessToken(Long userId);

  boolean isValidToken(String token, Long userId);

  String getAccessTokenDuration();

  String getRefreshTokenDuration();

  boolean isTokenExpired(String token);

  Long getUserIdFromToken(String token);

  String getTokenFromHeader(String authHeader);
}
