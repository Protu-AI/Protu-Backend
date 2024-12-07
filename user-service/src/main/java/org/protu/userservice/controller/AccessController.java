package org.protu.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.LoginRequestDto;
import org.protu.userservice.dto.RefreshResponseDto;
import org.protu.userservice.dto.RegisterRequestDto;
import org.protu.userservice.dto.TokensResponseDto;
import org.protu.userservice.service.JWTServiceImpl;
import org.protu.userservice.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class AccessController {

  private final UserServiceImpl userServiceImpl;
  private final JWTServiceImpl jwtServiceImpl;

  @PostMapping("/register")
  public ResponseEntity<TokensResponseDto> registerUser(@Validated @RequestBody RegisterRequestDto registerRequest) {
    TokensResponseDto tokensResponseDto = userServiceImpl.registerUser(registerRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(tokensResponseDto);
  }

  @PostMapping("/login")
  public ResponseEntity<TokensResponseDto> loginUser(@Validated @RequestBody LoginRequestDto loginRequestDto) {
    TokensResponseDto tokensResponseDto = userServiceImpl.loginUser(loginRequestDto);
    return ResponseEntity.status(HttpStatus.OK).body(tokensResponseDto);
  }

  @PostMapping("/refresh")
  public ResponseEntity<RefreshResponseDto> refreshAccessToken(@RequestHeader("Authorization") String authHeader) {
    String refreshToken = jwtServiceImpl.getTokenFromHeader(authHeader);
    Long authUserId = jwtServiceImpl.getUserIdFromToken(refreshToken);

    RefreshResponseDto refreshResponseDto = RefreshResponseDto.builder()
        .accessToken(jwtServiceImpl.generateAccessToken(authUserId))
        .expiresIn(jwtServiceImpl.getAccessTokenDuration())
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(refreshResponseDto);
  }
}
