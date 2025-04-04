package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.request.*;
import org.protu.userservice.dto.response.RefreshResDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.dto.response.ValidateIdentifierResDto;
import org.protu.userservice.dto.response.signUpResDto;
import org.protu.userservice.exceptions.custom.EmailNotVerifiedException;
import org.protu.userservice.exceptions.custom.UserEmailAlreadyVerifiedException;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.mapper.TokenMapper;
import org.protu.userservice.mapper.UserMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final TokenMapper tokenMapper;
  private final UserMapper userMapper;
  private final JWTService jwtService;
  private final UserHelper userHelper;
  private final RedisTemplate<Object, String> redisTemplate;
  private final OtpService otpService;
  private final AppProperties properties;
  private final String VERIFY_EMAIL = "email-verification";
  private final String PASSWORD_RESET = "password-reset";

  public signUpResDto signUpUser(SignUpReqDto signUpReqDto) {
    userHelper.checkIfUserExists(signUpReqDto.email(), "email");
    userHelper.checkIfUserExists(signUpReqDto.username(), "username");
    User user = userMapper.toUserEntity(signUpReqDto, passwordEncoder);
    userRepo.save(user);
    otpService.sendOtp(6, properties.otp().prefix().email() + user.getId(), user, properties.otp().emailTtl(), VERIFY_EMAIL);
    return new signUpResDto(user.getEmail(), true);
  }

  public TokensResDto verifyUserEmail(VerifyEmailReqDto requestDto) {
    User user = userHelper.fetchUserOrThrow(requestDto.email(), "email");
    if (user.getIsEmailVerified()) {
      throw new UserEmailAlreadyVerifiedException(FailureMessages.EMAIL_ALREADY_VERIFIED.getMessage());
    }

    otpService.verifyUserEnteredOtpOrThrow(properties.otp().prefix().email() + user.getId(), requestDto.OTP());
    return userHelper.markUserEmailVerified(user);
  }

  public ValidateIdentifierResDto validateUserIdentifier(String userIdentifier) {
    User user = userHelper.fetchUserOrThrow(userIdentifier, "username/email");
    if (!user.getIsEmailVerified()) {
      otpService.sendOtp(6, properties.otp().prefix().email() + user.getId(), user, properties.otp().emailTtl(), VERIFY_EMAIL);
      throw new EmailNotVerifiedException(FailureMessages.EMAIL_NOT_VERIFIED.getMessage(userIdentifier));
    }

    return new ValidateIdentifierResDto(user.getImageUrl());
  }

  public TokensResDto signIn(SignInReqDto signInReqDto) {
    User user = userHelper.fetchUserOrThrow(signInReqDto.userIdentifier(), "username/email");
    if (!user.getIsEmailVerified()) {
      otpService.sendOtp(6, properties.otp().prefix().email() + user.getId(), user, properties.otp().emailTtl(), VERIFY_EMAIL);
      throw new EmailNotVerifiedException(FailureMessages.EMAIL_NOT_VERIFIED.getMessage(signInReqDto.userIdentifier()));
    }
    if (!passwordEncoder.matches(signInReqDto.password(), user.getPassword())) {
      throw new BadCredentialsException(FailureMessages.BAD_CREDENTIALS.getMessage("password"));
    }
    return tokenMapper.toTokensDto(user, jwtService);
  }

  public RefreshResDto refreshAccessToken(String refreshToken) {
    String authUserId = jwtService.getUserIdFromToken(refreshToken);
    User user = userHelper.fetchUserByIdOrThrow(authUserId);
    return tokenMapper.toTokensDto(jwtService, user);
  }

  public void forgotPassword(SendOtpDto requestDto) {
    Optional<User> userOpt = userHelper.findUserByIdentifier(requestDto.email(), Optional.of("email"));
    if (userOpt.isEmpty())
      return;
    User user = userOpt.get();
    otpService.sendOtp(6, properties.otp().prefix().password() + user.getId(), user, properties.otp().passwordTtl(), PASSWORD_RESET);
  }

  public void verifyResetPasswordOtp(VerifyResetPasswordOtpDto requestDto) {
    User user = userHelper.fetchUserOrThrow(requestDto.email(), "email");
    otpService.verifyUserEnteredOtpOrThrow(properties.otp().prefix().password() + user.getId(), requestDto.OTP());
    redisTemplate.opsForValue().getAndDelete(properties.otp().prefix().password() + user.getId());
  }

  public void resetPassword(ResetPasswordReqDto requestDto) {
    User user = userHelper.fetchUserOrThrow(requestDto.email(), "email");
    user.setPassword(passwordEncoder.encode(requestDto.password()));
    userRepo.save(user);
    jwtService.blockCurrentUserTokens(user.getPublicId());
  }
}
