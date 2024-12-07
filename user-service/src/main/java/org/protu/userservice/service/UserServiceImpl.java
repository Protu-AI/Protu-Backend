package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.*;
import org.protu.userservice.exceptions.custom.UnauthorizedAccessException;
import org.protu.userservice.exceptions.custom.UserAlreadyExistsException;
import org.protu.userservice.exceptions.custom.UserNotFoundException;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JWTServiceImpl jwtService;

  private void verifyAuthority(Long userId, Long authUserId) {
    if (!userId.equals(authUserId)) {
      throw new UnauthorizedAccessException("You do not have permission to access this resource.");
    }
  }

  @Override
  public TokensResponseDto registerUser(RegisterRequestDto registerRequestDto) {
    if (userRepository.existsByUsername(registerRequestDto.getUsername())) {
      throw new UserAlreadyExistsException("User with username: " + registerRequestDto.getUsername() + " already exists");
    }

    if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
      throw new UserAlreadyExistsException("User with email: " + registerRequestDto.getEmail() + " already exists");
    }

    User user = User.builder()
        .username(registerRequestDto.getUsername())
        .firstName(registerRequestDto.getFirstName())
        .lastName(registerRequestDto.getLastName())
        .email(registerRequestDto.getEmail())
        .passwordHash(passwordEncoder.encode(registerRequestDto.getPassword()))
        .phoneNumber(registerRequestDto.getPhoneNumber())
        .authorities("ROLE_USER")
        .isActive(true)
        .build();

    userRepository.save(user);
    return TokensResponseDto.builder()
        .userId(user.getUserId())
        .accessToken(jwtService.generateAccessToken(user.getUserId()))
        .refreshToken(jwtService.generateRefreshToken(user.getUserId()))
        .refreshTokenExpiresIn(jwtService.getRefreshTokenDuration())
        .accessTokenExpiresIn(jwtService.getAccessTokenDuration())
        .tokenType("Bearer")
        .build();
  }

  @Override
  public TokensResponseDto loginUser(LoginRequestDto loginRequestDto) {
    User user = userRepository.findByUsername(loginRequestDto.getUsername())
        .orElseThrow(() -> new UserNotFoundException("User with username: " + loginRequestDto.getUsername() + " not found"));

    if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPasswordHash())) {
      throw new BadCredentialsException("Invalid username or password");
    }

    return TokensResponseDto.builder()
        .userId(user.getUserId())
        .accessToken(jwtService.generateAccessToken(user.getUserId()))
        .refreshToken(jwtService.generateRefreshToken(user.getUserId()))
        .refreshTokenExpiresIn(jwtService.getRefreshTokenDuration())
        .accessTokenExpiresIn(jwtService.getAccessTokenDuration())
        .tokenType("Bearer")
        .build();
  }

  @Override
  public UserResponseDto getUserById(Long userId, Long authUserId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

    verifyAuthority(userId, authUserId);
    return UserResponseDto.builder()
        .id(user.getUserId())
        .username(user.getUsername())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .build();
  }

  @Override
  public UserResponseDto updateUser(Long userId, Long authUserId, UpdateRequestDto updateRequestDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

    verifyAuthority(userId, authUserId);
    user.setUsername(updateRequestDto.getUsername());
    user.setFirstName(updateRequestDto.getFirstName());
    user.setLastName(updateRequestDto.getLastName());
    user.setEmail(updateRequestDto.getEmail());
    user.setPhoneNumber(updateRequestDto.getPhoneNumber());
    user.setPasswordHash(passwordEncoder.encode(updateRequestDto.getPassword()));

    userRepository.save(user);
    return UserResponseDto.builder()
        .id(user.getUserId())
        .username(user.getUsername())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .build();
  }

  @Override
  public void deactivateUser(Long userId, Long authUserId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

    verifyAuthority(userId, authUserId);
    user.setIsActive(false);
    userRepository.save(user);
  }

}
