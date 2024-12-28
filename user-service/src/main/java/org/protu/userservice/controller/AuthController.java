package org.protu.userservice.controller;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.dto.request.*;
import org.protu.userservice.dto.response.RefreshResDto;
import org.protu.userservice.dto.response.RegisterResDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.service.AuthService;
import org.protu.userservice.service.JWTService;
import org.protu.userservice.service.VerificationCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${api.version}/auth")
@RequiredArgsConstructor
public class AuthController {

  private final JWTService jwtService;
  private final AuthService authService;
  private final VerificationCodeService verificationCodeService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<RegisterResDto>> registerUser(@Validated @RequestBody RegisterReqDto registerRequest) {
    RegisterResDto registerResDto = authService.registerUser(registerRequest);
    String message = "User registration successful. If the verification email doesn't appear in your inbox, please check your spam folder or try again later.";
    return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(registerResDto, message));
  }

  @PostMapping("/confirm")
  public ResponseEntity<ApiResponse<TokensResDto>> verifyUserEmail(@Validated @RequestBody VerifyEmailReqDto requestDto) {
    TokensResDto responseDTO = verificationCodeService.verifyUserEmailAndCode(requestDto);
    String message = "Email verified successfully. Your account is now active";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(responseDTO, message));
  }

  @PostMapping("/validate-user")
  public ResponseEntity<ApiResponse<Void>> validateUserIdentifier(@RequestParam String userIdentifier) {
    authService.validateUserIdentifier(userIdentifier);
    String message = "The username or email you provided is valid. Please proceed to the next step.";
    return ResponseEntity.ok(new ApiResponse<>(null, message));
  }

  @PostMapping("/authenticate")
  public ResponseEntity<ApiResponse<TokensResDto>> authenticate(@Validated @RequestBody LoginReqDto loginRequest) {
    TokensResDto responseDTO = authService.authenticate(loginRequest);
    String message = "Welcome back! You have successfully logged in.";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(responseDTO, message));
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshResDto>> refreshAccessToken(@RequestHeader("Authorization") String authHeader) {
    String refreshToken = jwtService.getTokenFromHeader(authHeader);
    Long authUserId = jwtService.getUserIdFromToken(refreshToken);

    String message = "Access token has been refreshed successfully";
    RefreshResDto refreshResDto = RefreshResDto.builder()
        .accessToken(jwtService.generateAccessToken(authUserId))
        .expiresIn(jwtService.getAccessTokenDuration())
        .build();

    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(refreshResDto, message));
  }

  @GetMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(@Validated @RequestBody ForgotPasswordReqDto requestDto) {
    authService.forgotPassword(requestDto);
    String message = "If the provided email exists, a password reset verification code will be sent to your inbox. Please check your email to proceed.";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, message));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(@Validated @RequestBody ResetPasswordReqDto requestDto) {
    authService.resetPassword(requestDto);
    String message = "Password reset successfully";
    return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(null, message));
  }
}


