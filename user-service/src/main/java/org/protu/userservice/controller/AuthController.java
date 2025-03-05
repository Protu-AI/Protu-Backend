package org.protu.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.ApiProperties;
import org.protu.userservice.constants.SuccessMessages;
import org.protu.userservice.dto.ApiResponse;
import org.protu.userservice.dto.request.*;
import org.protu.userservice.dto.response.RefreshResDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.dto.response.ValidateIdentifierResDto;
import org.protu.userservice.dto.response.signUpResDto;
import org.protu.userservice.service.AuthService;
import org.protu.userservice.service.JWTService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.protu.userservice.helper.SuccessResponseHelper.buildResponse;

@RestController
@RequestMapping("/api/${api.version}/auth")
@RequiredArgsConstructor
public class AuthController {
  private final JWTService jwtService;
  private final AuthService authService;
  private final ApiProperties apiProperties;

  @PostMapping("/sign-up")
  public ResponseEntity<ApiResponse<signUpResDto>> signUpUser(@Validated @RequestBody SignUpReqDto signUpReqDto, HttpServletRequest request) {
    signUpResDto signUpResDto = authService.signUpUser(signUpReqDto);
    return buildResponse(apiProperties.getVersion(), request, HttpStatus.CREATED, signUpResDto, SuccessMessages.SIGN_UP_MSG.message);
  }

  @PostMapping("/verify-email")
  public ResponseEntity<ApiResponse<TokensResDto>> verifyUserEmail(@Validated @RequestBody VerifyEmailReqDto requestDto, HttpServletRequest request) {
    TokensResDto responseDTO = authService.verifyUserEmail(requestDto);
    return buildResponse(apiProperties.getVersion(), request, HttpStatus.OK, responseDTO, SuccessMessages.VERIFY_MSG.message);
  }

  @PostMapping("/validate-identifier")
  public ResponseEntity<ApiResponse<ValidateIdentifierResDto>> validateUserIdentifier(@RequestParam String userIdentifier, HttpServletRequest request) {
    ValidateIdentifierResDto responseDto = authService.validateUserIdentifier(userIdentifier);
    return buildResponse(apiProperties.getVersion(), request, HttpStatus.OK, responseDto, SuccessMessages.VALIDATE_MSG.message);
  }

  @PostMapping("/sign-in")
  public ResponseEntity<ApiResponse<TokensResDto>> signIn(@Validated @RequestBody SignInReqDto signInReqDto, HttpServletRequest request) {
    TokensResDto responseDTO = authService.signIn(signInReqDto);
    return buildResponse(apiProperties.getVersion(), request, HttpStatus.OK, responseDTO, SuccessMessages.SIGN_IN_MSG.message);
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<RefreshResDto>> refreshAccessToken(@RequestHeader("Authorization") String authHeader, HttpServletRequest request) {
    String refreshToken = jwtService.getTokenFromHeader(authHeader);
    RefreshResDto refreshResDto = authService.refreshAccessToken(refreshToken);
    return buildResponse(apiProperties.getVersion(), request, HttpStatus.OK, refreshResDto, SuccessMessages.REFRESH_MSG.message);
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<Void>> forgotPassword(@Validated @RequestBody SendOtpDto requestDto, HttpServletRequest request) {
    authService.forgotPassword(requestDto);
    return buildResponse(apiProperties.getVersion(), request, HttpStatus.OK, null, SuccessMessages.FORGOT_PASSWORD_MSG.message);
  }

  @PostMapping("/verify-password-otp")
  public ResponseEntity<ApiResponse<Void>> verifyResetPasswordOtp(@Validated @RequestBody VerifyResetPasswordOtpDto requestDto, HttpServletRequest request) {
    authService.verifyResetPasswordOtp(requestDto);
    return buildResponse(apiProperties.getVersion(), request, HttpStatus.OK, null, SuccessMessages.PASSWORD_RESET_OTP_MSG.message);
  }

  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(@Validated @RequestBody ResetPasswordReqDto requestDto, HttpServletRequest request) {
    authService.resetPassword(requestDto);
    return buildResponse(apiProperties.getVersion(), request, HttpStatus.OK, null, SuccessMessages.RESET_PASSWORD_MSG.message);
  }
}
