package org.protu.userservice.service.impl;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.request.LoginReqDto;
import org.protu.userservice.dto.request.RegisterReqDto;
import org.protu.userservice.dto.request.UpdateReqDto;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.RegisterResDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.dto.response.UserResDto;
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
  public RegisterResDto registerUser(RegisterReqDto registerReqDto) {
    Optional<User> userOpt = userRepository.findByUsername(registerReqDto.getUsername());
    if (userOpt.isPresent()) {
      throw new UserAlreadyExistsException("User with username: " + registerReqDto.getUsername() + " already exists");
    }

    userOpt = userRepository.findByEmail(registerReqDto.getEmail());
    if (userOpt.isPresent()) {
      if (userOpt.get().getIsEmailVerified()) {
        throw new UserAlreadyExistsException("User with email: " + registerReqDto.getEmail() + " already exists");
      }

      verificationCodeService.sendVerificationCode(userOpt.get());
      throw new UserEmailNotVerifiedException("This email is already registered but not verified. A new verification email has been sent to your inbox.");
    }

    User user = userMapper.registerReqDtoToUser(registerReqDto);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setAuthorities("ROLE_USER");
    user.setIsActive(true);
    user.setIsEmailVerified(false);
    verificationCodeService.sendVerificationCode(user);
    userRepository.save(user);

    return RegisterResDto.builder()
        .email(user.getEmail())
        .emailSent(true)
        .build();
  }


  @Override
  public TokensResDto loginUser(LoginReqDto loginReqDto) {
    User user = userRepository.findByUsername(loginReqDto.getUsername())
        .orElseThrow(() -> new UserNotFoundException("User with username: " + loginReqDto.getUsername() + " is not found"));

    if (!passwordEncoder.matches(loginReqDto.getPassword(), user.getPassword())) {
      throw new BadCredentialsException("Invalid username or password");
    }

    if (!user.getIsEmailVerified()) {
      verificationCodeService.sendVerificationCode(user);
      throw new UserEmailNotVerifiedException("Your email is not verified. Please check your email to verify your account.");
    }

    return TokensResDto.builder()
        .userId(user.getId())
        .accessToken(jwtService.generateAccessToken(user.getId()))
        .refreshToken(jwtService.generateRefreshToken(user.getId()))
        .refreshTokenExpiresIn(jwtService.getRefreshTokenDuration())
        .accessTokenExpiresIn(jwtService.getAccessTokenDuration())
        .tokenType("Bearer")
        .build();
  }

  @Override
  public UserResDto getUserById(Long userId, Long authUserId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " is not found"));

    verifyUserAuthority(userId, authUserId);
    return userMapper.userToUserResDto(user);
  }

  @Override
  public UserResDto updateUser(Long userId, Long authUserId, UpdateReqDto updateReqDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " is not found"));

    verifyUserAuthority(userId, authUserId);
    user.setUsername(updateReqDto.getUsername());
    user.setFirstName(updateReqDto.getFirstName());
    user.setLastName(updateReqDto.getLastName());
    user.setPhoneNumber(updateReqDto.getPhoneNumber());
    user.setPassword(passwordEncoder.encode(updateReqDto.getPassword()));

    userRepository.save(user);
    return userMapper.userToUserResDto(user);
  }

  @Override
  public DeactivateResDto deactivateUser(Long userId, Long authUserId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new UserNotFoundException("User with id: " + userId + " is not found"));

    verifyUserAuthority(userId, authUserId);
    user.setIsActive(false);
    userRepository.save(user);
    return userMapper.UserToDeactivateResDto(user);
  }

}
