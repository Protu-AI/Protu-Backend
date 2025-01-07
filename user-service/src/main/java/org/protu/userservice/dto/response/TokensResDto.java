package org.protu.userservice.dto.response;

public record TokensResDto (Long userId, String accessToken, String refreshToken, String accessTokenExpiresIn, String refreshTokenExpiresIn, String tokenType){}
