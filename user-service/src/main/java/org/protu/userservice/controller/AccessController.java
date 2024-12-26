package org.protu.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.dto.request.*;
import org.protu.userservice.dto.response.RefreshResDto;
import org.protu.userservice.dto.response.RegisterResDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.service.impl.JWTServiceImpl;
import org.protu.userservice.service.impl.UserServiceImpl;
import org.protu.userservice.service.impl.VerificationCodeServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/auth")
@RequiredArgsConstructor
public class AccessController {

  private final UserServiceImpl userServiceImpl;
  private final JWTServiceImpl jwtServiceImpl;
  private final VerificationCodeServiceImpl verificationCodeServiceImpl;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResDto>> registerUser(@Validated @RequestBody RegisterReqDto registerRequest) {
    RegisterResDto registerResDto = userServiceImpl.registerUser(registerRequest);
    String message = "User registration successful. If the verification email doesn't appear in your inbox, please check your spam folder or try again later.";
    return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(registerResDto, message));
  }

  @PostMapping("/confirm")
  public ResponseEntity<ApiResponse<TokensResDto>> verifyUserEmail(@Validated @RequestBody VerifyEmailReqDto requestDto) {
    TokensResDto responseDTO = verificationCodeServiceImpl.verifyUserEmailAndCode(requestDto);
    String message = "Email verified successfully. Your account is now active";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(responseDTO, message));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<TokensResDto>> loginUser(@Validated @RequestBody LoginReqDto loginReqDto) {
    TokensResDto tokensResDto = userServiceImpl.loginUser(loginReqDto);
    String message = "Welcome back! You have successfully logged in.";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(tokensResDto, message));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshResDto>> refreshAccessToken(@RequestHeader("Authorization") String authHeader) {
    String refreshToken = jwtServiceImpl.getTokenFromHeader(authHeader);
    Long authUserId = jwtServiceImpl.getUserIdFromToken(refreshToken);

    String message = "Access token has been refreshed successfully";
    RefreshResDto refreshResDto = RefreshResDto.builder()
        .accessToken(jwtServiceImpl.generateAccessToken(authUserId))
        .expiresIn(jwtServiceImpl.getAccessTokenDuration())
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(refreshResDto, message));
  }

  @GetMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(@Validated @RequestBody ForgotPasswordReqDto requestDto) {
    userServiceImpl.forgotPassword(requestDto);
    String message = "If the provided email exists, a password reset verification code will be sent to your inbox. Please check your email to proceed.";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, message));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(@Validated @RequestBody ResetPasswordReqDto requestDto) {
    userServiceImpl.resetPassword(requestDto);
    String message = "Password reset successfully";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, message));
  }
}


