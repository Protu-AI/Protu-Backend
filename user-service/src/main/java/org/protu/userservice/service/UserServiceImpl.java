package org.protu.userservice.service;

import org.protu.userservice.dto.*;
import org.protu.userservice.exceptions.custom.UserAlreadyExistsException;
import org.protu.userservice.exceptions.custom.UserNotFoundException;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JWTServiceImpl jwtService;

  public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JWTServiceImpl jwtService) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
  }

  private String getAccessToken(String username) {
    return jwtService.generateAccessToken(username);
  }

  private String getRefreshToken(String username) {
    return jwtService.generateRefreshToken(username);
  }

  private void verifyAuthority(String username1, String username2) throws AccessDeniedException {
    if (!username1.equals(username2)) {
      throw new AccessDeniedException("You do not have permission to access this resource.");
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
        .accessToken(getAccessToken(user.getUsername()))
        .refreshToken(getRefreshToken(user.getUsername()))
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
        .accessToken(getAccessToken(loginRequestDto.getUsername()))
        .refreshToken(getRefreshToken(loginRequestDto.getUsername()))
        .build();
  }

  @Override
  public UserResponseDto getUserById(Long userId, String username) throws AccessDeniedException {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));
    verifyAuthority(user.getUsername(), username);

    return UserResponseDto.builder()
        .id(user.getUserId())
        .username(user.getUsername())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .email(user.getEmail())
        .phoneNumber(user.getPhoneNumber())
        .password(user.getPasswordHash())
        .build();
  }

  @Override
  public UserResponseDto updateUser(Long userId, UpdateRequestDto updateRequestDto, String username) throws AccessDeniedException {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));
    verifyAuthority(user.getUsername(), updateRequestDto.getUsername());

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
        .password(user.getPasswordHash())
        .build();
  }

  @Override
  public void deactivateUser(Long userId, String username) throws AccessDeniedException {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " not found"));

    verifyAuthority(user.getUsername(), username);
    user.setIsActive(false);
    userRepository.save(user);
  }

}
