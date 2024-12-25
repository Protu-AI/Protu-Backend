package org.protu.userservice.service;

import org.protu.userservice.dto.*;

import java.nio.file.AccessDeniedException;

public interface UserService {
  SignupResponseDto registerUser(RegisterRequestDto registerRequest);

  TokensResponseDto loginUser(LoginRequestDto loginRequestDto);

  void verifyUserAuthority(Long userId, Long authUserId);

  UserResponseDto getUserById(Long userId, Long authUserId) throws AccessDeniedException;

  UserResponseDto updateUser(Long userId, Long authUserId, UpdateRequestDto userUpdateDto) throws AccessDeniedException;

  void deactivateUser(Long userId, Long authUserId) throws AccessDeniedException;
}
