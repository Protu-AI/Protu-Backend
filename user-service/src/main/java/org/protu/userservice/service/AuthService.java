package org.protu.userservice.service;

import org.protu.userservice.config.AppProperties;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.rabbitmq.RabbitMessage;
import org.protu.userservice.dto.rabbitmq.UserData;
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
import org.protu.userservice.producer.UserEventsProducer;
import org.protu.userservice.repository.UserRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final TokenMapper tokenMapper;
  private final UserMapper userMapper;
  private final JWTService jwtService;
  private final UserHelper userHelper;
  private final RedisTemplate<Object, String> redisTemplate;
  private final OtpService otpService;
  private final String VERIFY_EMAIL = "email-verification";
  private final String PASSWORD_RESET = "password-reset";
  private final String EMAIL_PREFIX;
  private final String PASSWORD_PREFIX;
  private final long EMAIL_TTL;
  private final long PASSWORD_TTL;

  private final UserEventsProducer userEventsProducer;
  private final String USER_CREATED;

  public AuthService(UserRepository userRepo, PasswordEncoder passwordEncoder, TokenMapper tokenMapper, UserMapper userMapper, JWTService jwtService, UserHelper userHelper, RedisTemplate<Object, String> redisTemplate, OtpService otpService, AppProperties props, UserEventsProducer userEventsProducer) {
    this.userRepo = userRepo;
    this.passwordEncoder = passwordEncoder;
    this.tokenMapper = tokenMapper;
    this.userMapper = userMapper;
    this.jwtService = jwtService;
    this.userHelper = userHelper;
    this.redisTemplate = redisTemplate;
    this.otpService = otpService;
    this.userEventsProducer = userEventsProducer;
    EMAIL_PREFIX = props.otp().prefix().email();
    EMAIL_TTL = props.otp().emailTtl();
    PASSWORD_PREFIX = props.otp().prefix().password();
    PASSWORD_TTL = props.otp().passwordTtl();
    USER_CREATED = props.rabbit().routingKey().userCreated();
  }

  public signUpResDto signUpUser(SignUpReqDto signUpReqDto) {
    userHelper.checkIfUserExists(signUpReqDto.email(), "email");
    userHelper.checkIfUserExists(signUpReqDto.username(), "username");
    User user = userMapper.toUserEntity(signUpReqDto, passwordEncoder);
    userRepo.save(user);
    otpService.sendOtp(6, EMAIL_PREFIX + user.getId(), user, EMAIL_TTL, VERIFY_EMAIL);
    userEventsProducer.send(
        new RabbitMessage<>(USER_CREATED, "event",
            new UserData(user.getId(), user.getPublicId(), user.getRoles())), USER_CREATED);
    return new signUpResDto(user.getEmail(), true);
  }

  public TokensResDto verifyUserEmail(VerifyEmailReqDto requestDto) {
    User user = userHelper.fetchUserOrThrow(requestDto.email(), "email");
    if (user.getIsEmailVerified()) {
      throw new UserEmailAlreadyVerifiedException(FailureMessages.EMAIL_ALREADY_VERIFIED.getMessage());
    }

    otpService.verifyUserEnteredOtpOrThrow(EMAIL_PREFIX + user.getId(), requestDto.OTP());
    return userHelper.markUserEmailVerified(user);
  }

  public ValidateIdentifierResDto validateUserIdentifier(String userIdentifier) {
    User user = userHelper.fetchUserOrThrow(userIdentifier, "username/email");
    if (!user.getIsEmailVerified()) {
      otpService.sendOtp(6, EMAIL_PREFIX + user.getId(), user, EMAIL_TTL, VERIFY_EMAIL);
      throw new EmailNotVerifiedException(FailureMessages.EMAIL_NOT_VERIFIED.getMessage(userIdentifier));
    }

    return new ValidateIdentifierResDto(user.getImageUrl());
  }

  public TokensResDto signIn(SignInReqDto signInReqDto) {
    User user = userHelper.fetchUserOrThrow(signInReqDto.userIdentifier(), "username/email");
    if (!user.getIsEmailVerified()) {
      otpService.sendOtp(6, EMAIL_PREFIX + user.getId(), user, EMAIL_TTL, VERIFY_EMAIL);
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
    otpService.sendOtp(6, PASSWORD_PREFIX + user.getId(), user, PASSWORD_TTL, PASSWORD_RESET);
  }

  public void verifyResetPasswordOtp(VerifyResetPasswordOtpDto requestDto) {
    User user = userHelper.fetchUserOrThrow(requestDto.email(), "email");
    otpService.verifyUserEnteredOtpOrThrow(PASSWORD_PREFIX + user.getId(), requestDto.OTP());
    redisTemplate.opsForValue().getAndDelete(PASSWORD_PREFIX + user.getId());
  }

  public void resetPassword(ResetPasswordReqDto requestDto) {
    User user = userHelper.fetchUserOrThrow(requestDto.email(), "email");
    user.setPassword(passwordEncoder.encode(requestDto.password()));
    userRepo.save(user);
    jwtService.blockCurrentUserTokens(user.getPublicId());
  }
}
