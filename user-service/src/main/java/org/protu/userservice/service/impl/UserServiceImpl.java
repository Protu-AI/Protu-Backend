package org.protu.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.*;
import org.protu.userservice.exceptions.custom.UnauthorizedAccessException;
import org.protu.userservice.exceptions.custom.UserAlreadyExistsException;
import org.protu.userservice.exceptions.custom.UserEmailNotVerifiedException;
import org.protu.userservice.exceptions.custom.UserNotFoundException;
import org.protu.userservice.mapper.UserMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.protu.userservice.service.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JWTServiceImpl jwtService;
  private final UserMapper userMapper;
  private final VerificationCodeServiceImpl verificationCodeService;

  @Override
  public void verifyUserAuthority(Long userId, Long authUserId) {
    if (!userId.equals(authUserId)) {
      throw new UnauthorizedAccessException("You do not have permission to access this resource.");
    }
  }

  @Override
  public SignupResponseDto registerUser(RegisterRequestDto registerRequestDto) {
    Optional<User> userOpt = userRepository.findByUsername(registerRequestDto.getUsername());
    if (userOpt.isPresent()) {
      throw new UserAlreadyExistsException("User with username: " + registerRequestDto.getUsername() + " already exists");
    }

    userOpt = userRepository.findByEmail(registerRequestDto.getEmail());
    if (userOpt.isPresent()) {
      if (userOpt.get().getIsEmailVerified()) {
        throw new UserAlreadyExistsException("User with email: " + registerRequestDto.getEmail() + " already exists");
      }

      verificationCodeService.sendVerificationCode(userOpt.get());
      throw new UserEmailNotVerifiedException("This email is already registered but not verified. A new verification email has been sent to your inbox.");
    }

    User user = userMapper.registerRequestDtoToUser(registerRequestDto);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setAuthorities("ROLE_USER");
    user.setIsActive(true);
    user.setIsEmailVerified(false);
    verificationCodeService.sendVerificationCode(user);
    userRepository.save(user);

    return SignupResponseDto.builder()
        .message("Signup successful. Please check your email to verify your account.")
        .email(user.getEmail())
        .emailSent(true)
        .build();
  }


  @Override
  public TokensResponseDto loginUser(LoginRequestDto loginRequestDto) {
    User user = userRepository.findByUsername(loginRequestDto.getUsername())
        .orElseThrow(() -> new UserNotFoundException("User with username: " + loginRequestDto.getUsername() + " not found"));

    if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid username or password");
    }

    if (!user.getIsEmailVerified()) {
      verificationCodeService.sendVerificationCode(user);
      throw new UserEmailNotVerifiedException("Your email is not verified. Please check your email to verify your account.");
    }

    return TokensResponseDto.builder()
        .userId(user.getId())
        .accessToken(jwtService.generateAccessToken(user.getId()))
        .refreshToken(jwtService.generateRefreshToken(user.getId()))
        .refreshTokenExpiresIn(jwtService.getRefreshTokenDuration())
        .accessTokenExpiresIn(jwtService.getAccessTokenDuration())
        .tokenType("Bearer")
        .build();
  }

  @Override
  public UserResponseDto getUserById(Long userId, Long authUserId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

    verifyUserAuthority(userId, authUserId);
    return userMapper.userToUserResponseDto(user);
  }

  @Override
  public UserResponseDto updateUser(Long userId, Long authUserId, UpdateRequestDto updateRequestDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

    verifyUserAuthority(userId, authUserId);
    user.setUsername(updateRequestDto.getUsername());
    user.setFirstName(updateRequestDto.getFirstName());
    user.setLastName(updateRequestDto.getLastName());
    user.setPhoneNumber(updateRequestDto.getPhoneNumber());
    user.setPassword(passwordEncoder.encode(updateRequestDto.getPassword()));

    userRepository.save(user);
    return userMapper.userToUserResponseDto(user);
  }

  @Override
  public void deactivateUser(Long userId, Long authUserId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

    verifyUserAuthority(userId, authUserId);
    user.setIsActive(false);
    userRepository.save(user);
  }

}
