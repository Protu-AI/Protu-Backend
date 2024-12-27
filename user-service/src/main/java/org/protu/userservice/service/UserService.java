package org.protu.userservice.service;

import org.protu.userservice.dto.request.UpdateReqDto;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserResDto;

import java.nio.file.AccessDeniedException;

public interface UserService {
  void verifyUserAuthority(Long userId, Long authUserId);

  UserResDto getUserById(Long userId, Long authUserId) throws AccessDeniedException;

  UserResDto updateUser(Long userId, Long authUserId, UpdateReqDto userUpdateDto) throws AccessDeniedException;

  DeactivateResDto deactivateUser(Long userId, Long authUserId) throws AccessDeniedException;
}
