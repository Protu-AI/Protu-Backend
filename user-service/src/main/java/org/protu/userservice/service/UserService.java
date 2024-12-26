package org.protu.userservice.service;

import org.protu.userservice.dto.request.*;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.RegisterResDto;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.dto.response.UserResDto;

import java.nio.file.AccessDeniedException;

public interface UserService {
  RegisterResDto registerUser(RegisterReqDto registerRequest);

  TokensResDto loginUser(LoginReqDto loginReqDto);

  void verifyUserAuthority(Long userId, Long authUserId);

  UserResDto getUserById(Long userId, Long authUserId) throws AccessDeniedException;

  UserResDto updateUser(Long userId, Long authUserId, UpdateReqDto userUpdateDto) throws AccessDeniedException;

  DeactivateResDto deactivateUser(Long userId, Long authUserId) throws AccessDeniedException;

  void forgotPassword(ForgotPasswordReqDto requestDto);

  void resetPassword(ResetPasswordReqDto requestDto);
}
