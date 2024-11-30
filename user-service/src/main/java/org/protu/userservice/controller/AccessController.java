package org.protu.userservice.controller;

import org.protu.userservice.dto.LoginRequestDto;
import org.protu.userservice.dto.RegisterRequestDto;
import org.protu.userservice.dto.TokensResponseDto;
import org.protu.userservice.service.UserServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class AccessController {

  private final UserServiceImpl userServiceImpl;

  public AccessController(UserServiceImpl userServiceImpl) {
    this.userServiceImpl = userServiceImpl;
  }
  
  @PostMapping("/register")
  public ResponseEntity<TokensResponseDto> registerUser(@Validated @RequestBody RegisterRequestDto registerRequest) {
    TokensResponseDto tokenList = userServiceImpl.registerUser(registerRequest);
    return ResponseEntity.status(HttpStatus.CREATED).body(tokenList);
  }

  @PostMapping("/login")
  public ResponseEntity<TokensResponseDto> loginUser(@Validated @RequestBody LoginRequestDto loginRequestDto) {
    TokensResponseDto tokensResponseDto = userServiceImpl.loginUser(loginRequestDto);
    return ResponseEntity.status(HttpStatus.OK).body(tokensResponseDto);
  }
}
