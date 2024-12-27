package org.protu.userservice.mapper;

import org.mapstruct.*;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.model.User;
import org.protu.userservice.service.impl.JWTServiceImpl;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TokenMapper {

  @Mappings({
      @Mapping(source = "id", target = "userId"),
      @Mapping(target = "accessToken", expression = "java(jwtService.generateAccessToken(user.getId()))"),
      @Mapping(target = "refreshToken", expression = "java(jwtService.generateRefreshToken(user.getId()))"),
      @Mapping(target = "accessTokenExpiresIn", expression = "java(jwtService.getAccessTokenDuration())"),
      @Mapping(target = "refreshTokenExpiresIn", expression = "java(jwtService.getRefreshTokenDuration())"),
      @Mapping(target = "tokenType", constant = "Bearer")
  })
  TokensResDto userToTokensResDto(User user, @Context JWTServiceImpl jwtService);
}
