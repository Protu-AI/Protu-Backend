package org.protu.userservice.dto.response;

public record TokensResDto (String publicId, String accessToken, String refreshToken, String accessTokenExpiresIn, String refreshTokenExpiresIn, String tokenType){}
