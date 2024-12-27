package org.protu.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.request.ForgotPasswordReqDto;
import org.protu.userservice.dto.request.LoginReqDto;
import org.protu.userservice.dto.request.RegisterReqDto;
import org.protu.userservice.dto.request.ResetPasswordReqDto;
import org.protu.userservice.dto.response.RegisterResDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.exceptions.custom.UserAlreadyExistsException;
import org.protu.userservice.exceptions.custom.UserEmailNotVerifiedException;
import org.protu.userservice.exceptions.custom.UserNotFoundException;
import org.protu.userservice.mapper.TokenMapper;
import org.protu.userservice.mapper.UserMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.protu.userservice.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final JWTServiceImpl jwtService;
  private final TokenMapper tokenMapper;
  private final UserMapper userMapper;
  private final VerificationCodeServiceImpl verificationCodeService;

  private Optional<User> findUser(String identifier, Optional<String> type) {
    if (type.isEmpty())
      return userRepo.findByUsername(identifier).or(() -> userRepo.findByEmail(identifier));
    return type.get().equals("email") ? userRepo.findByEmail(identifier) : userRepo.findByUsername(identifier);
  }

  @Override
  public RegisterResDto registerUser(RegisterReqDto registerReqDto) {
    Optional<User> userOpt = findUser(registerReqDto.getEmail(), Optional.of("email"));
    if (userOpt.isPresent()) {
      throw new UserAlreadyExistsException("User with email: " + registerReqDto.getEmail() + " already exists");
    }

    userOpt = findUser(registerReqDto.getUsername(), Optional.of("username"));
    if (userOpt.isPresent()) {
      throw new UserAlreadyExistsException("User with username: " + registerReqDto.getUsername() + " already exists");
    }

    User user = userMapper.registerReqDtoToUser(registerReqDto, passwordEncoder);
    verificationCodeService.sendVerificationCode(user, "Verify your email");
    userRepo.save(user);
    return new RegisterResDto(user.getEmail(), true);
  }

  @Override
  public void validateUserIdentifier(String userIdentifier) {
    User user = findUser(userIdentifier, Optional.empty())
        .orElseThrow(() -> new BadCredentialsException("Incorrect username/email. Please check and try again."));
    if (user.getIsEmailVerified()) {
      verificationCodeService.sendVerificationCode(user, "Verify your email");
      throw new UserEmailNotVerifiedException("Your email is registered but not verified. A new verification email has been sent to your inbox.");
    }
  }

  @Override
  public TokensResDto authenticate(LoginReqDto loginReqDto) {
    User user = findUser(loginReqDto.getUserIdentifier(), Optional.empty())
        .orElseThrow(() -> new BadCredentialsException("Incorrect username/email. Please check and try again."));
    if (!passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Incorrect password. Please check and try again.");
    }

    return tokenMapper.userToTokensResDto(user, jwtService);
  }

  @Override
  public void forgotPassword(ForgotPasswordReqDto requestDto) {
    Optional<User> userOpt = userRepo.findByEmail(requestDto.getEmail());
    if (userOpt.isEmpty()) {
      throw new UserNotFoundException("User with email: " + requestDto.getEmail() + " is not found");
    }

    verificationCodeService.sendVerificationCode(userOpt.get(), "Reset your password");
  }

  @Override
  public void resetPassword(ResetPasswordReqDto requestDto) {
    Optional<User> userOpt = userRepo.findByEmail(requestDto.getEmail());
    if (userOpt.isEmpty()) {
      throw new UserNotFoundException("User with email: " + requestDto.getEmail() + " is not found");
    }

    verificationCodeService.validateCode(userOpt.get(), requestDto.getVerificationCode());
    User user = userOpt.get();
    user.setCodeExpiryDate(Timestamp.from(Instant.now()));
    user.setPassword(requestDto.getPassword());
    userRepo.save(user);
  }
}
