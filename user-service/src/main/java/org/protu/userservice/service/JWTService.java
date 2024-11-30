package org.protu.userservice.service;

public interface JWTService {

  public String generateRefreshToken(String username);

  public String generateAccessToken(String username);

  public boolean validateToken(String token, String username);

  public String getUsernameFromToken(String token);
}
