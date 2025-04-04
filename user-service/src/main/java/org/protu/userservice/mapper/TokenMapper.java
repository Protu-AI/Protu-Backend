package org.protu.userservice.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.protu.userservice.dto.response.RefreshResDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.model.User;
import org.protu.userservice.service.JWTService;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TokenMapper {

  @Mapping(target = "accessToken", expression = "java(jwtService.generateAccessToken(user))")
  @Mapping(target = "refreshToken", expression = "java(jwtService.generateRefreshToken(user))")
  @Mapping(target = "accessTokenExpiresIn", expression = "java(jwtService.getAccessTokenDuration())")
  @Mapping(target = "refreshTokenExpiresIn", expression = "java(jwtService.getRefreshTokenDuration())")
  @Mapping(target = "tokenType", constant = "Bearer")
  TokensResDto toTokensDto(User user, @Context JWTService jwtService);

  @Mapping(target = "accessToken", expression = "java(jwtService.generateAccessToken(user))")
  @Mapping(target = "expiresIn", expression = "java(jwtService.getAccessTokenDuration())")
  RefreshResDto toTokensDto(@Context JWTService jwtService, User user);
}
