package org.protu.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.*;
import org.protu.userservice.service.impl.JWTServiceImpl;
import org.protu.userservice.service.impl.UserServiceImpl;
import org.protu.userservice.service.impl.VerificationCodeServiceImpl;
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
  private final VerificationCodeServiceImpl verificationCodeServiceImpl;

  @PostMapping("/register")
  public ResponseEntity<SignupResponseDto> registerUser(@Validated @RequestBody RegisterRequestDto registerRequest) {
    SignupResponseDto signupResponseDto = userServiceImpl.registerUser(registerRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(signupResponseDto);
  }

  @PostMapping("/confirm")
  public ResponseEntity<TokensResponseDto> verifyUserEmail(@Validated @RequestBody VerificationRequestDTO requestDto) {
    TokensResponseDto responseDTO = verificationCodeServiceImpl.verifyUserEmailAndCode(requestDto);
    return ResponseEntity.status(HttpStatus.OK).body(responseDTO);
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


