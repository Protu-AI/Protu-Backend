package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.request.ResetPasswordReqDto;
import org.protu.userservice.dto.request.SendCodeDto;
import org.protu.userservice.dto.request.SignInReqDto;
import org.protu.userservice.dto.request.SignUpReqDto;
import org.protu.userservice.dto.response.RefreshResDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.dto.response.signUpResDto;
import org.protu.userservice.exceptions.custom.UserEmailNotVerifiedException;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.mapper.TokenMapper;
import org.protu.userservice.mapper.UserMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final JWTService jwtService;
  private final TokenMapper tokenMapper;
  private final UserMapper userMapper;
  private final UserHelper userHelper;
  private final VerificationCodeService verificationCodeService;

  public signUpResDto signUpUser(SignUpReqDto signUpReqDto) {
    userHelper.checkIfUserExists(signUpReqDto.email(), "email");
    userHelper.checkIfUserExists(signUpReqDto.username(), "username");
    User user = userMapper.signUpReqDtoToUser(signUpReqDto, passwordEncoder);
    verificationCodeService.sendVerificationCode(user, "Verify your email");
    userRepo.save(user);
    return new signUpResDto(user.getEmail(), true);
  }

  public void validateUserIdentifier(String userIdentifier) {
    User user = userHelper.fetchUserOrThrow(userIdentifier, "username/email");
    if (!user.getIsEmailVerified()) {
      verificationCodeService.sendVerificationCode(user, "Verify your email");
      throw new UserEmailNotVerifiedException(FailureMessages.EMAIL_NOT_VERIFIED.getMessage(userIdentifier));
    }
  }

  public TokensResDto signIn(SignInReqDto signInReqDto) {
    User user = userHelper.fetchUserOrThrow(signInReqDto.userIdentifier(), "username/email");
    if (!user.getIsEmailVerified()) {
      verificationCodeService.sendVerificationCode(user, "Verify your email");
      throw new UserEmailNotVerifiedException(FailureMessages.EMAIL_NOT_VERIFIED.getMessage(signInReqDto.userIdentifier()));
    }

    if (!passwordEncoder.matches(signInReqDto.password(), user.getPassword())) {
      throw new BadCredentialsException(FailureMessages.BAD_CREDENTIALS.getMessage("password", signInReqDto.password()));
    }

    return tokenMapper.userToTokensResDto(user, jwtService);
  }

  public RefreshResDto refreshAccessToken(String refreshToken) {
    Long authUserId = jwtService.getUserIdFromToken(refreshToken);
    return tokenMapper.authUserIdsToRefreshResDto(authUserId, jwtService);
  }

  public void forgotPassword(SendCodeDto requestDto) {
    sendNewCode(requestDto, "Reset your password");
  }

  public void resetPassword(ResetPasswordReqDto requestDto) {
    User user = userHelper.fetchUserOrThrow(requestDto.email(), "email");
    verificationCodeService.validateCode(user, requestDto.verificationCode());
    user.setCodeExpiryDate(Timestamp.from(Instant.now()));
    user.setPassword(passwordEncoder.encode(requestDto.password()));
    userRepo.save(user);
  }

  public void sendNewCode(SendCodeDto requestDto, String subject) {
    User user = userHelper.fetchUserOrThrow(requestDto.email(), "email");
    verificationCodeService.sendVerificationCode(user, subject);
  }
}
