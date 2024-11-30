package org.protu.userservice.service;

import org.protu.userservice.dto.*;

import java.nio.file.AccessDeniedException;

public interface UserService {
  TokensResponseDto registerUser(RegisterRequestDto registerRequest);

  TokensResponseDto loginUser(LoginRequestDto loginRequestDto);

  UserResponseDto getUserById(Long userId, String authHeader) throws AccessDeniedException;

  UserResponseDto updateUser(Long userId, UpdateRequestDto userUpdateDto, String authHeader) throws AccessDeniedException;

  void deactivateUser(Long userId, String authHeader) throws AccessDeniedException;
}
