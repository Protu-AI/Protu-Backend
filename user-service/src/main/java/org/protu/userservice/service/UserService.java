package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.request.UpdateReqDto;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserResDto;
import org.protu.userservice.exceptions.custom.UnauthorizedAccessException;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.mapper.UserMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepository userRepo;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final UserHelper userHelper;

  public void verifyUserAuthority(Long userId, Long authUserId) {
    if (!userId.equals(authUserId)) {
      throw new UnauthorizedAccessException(FailureMessages.UNAUTHORIZED_ACCESS.getMessage());
    }
  }

  public UserResDto getUserById(Long userId, Long authUserId) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    verifyUserAuthority(userId, authUserId);
    return userMapper.userToUserResDto(user);
  }

  public UserResDto updateUser(Long userId, Long authUserId, UpdateReqDto updateReqDto) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    verifyUserAuthority(userId, authUserId);
    user.setUsername(updateReqDto.getUsername());
    user.setFirstName(updateReqDto.getFirstName());
    user.setLastName(updateReqDto.getLastName());
    user.setPhoneNumber(updateReqDto.getPhoneNumber());
    user.setPassword(passwordEncoder.encode(updateReqDto.getPassword()));

    userRepo.save(user);
    return userMapper.userToUserResDto(user);
  }

  public DeactivateResDto deactivateUser(Long userId, Long authUserId) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    verifyUserAuthority(userId, authUserId);
    user.setIsActive(false);
    userRepo.save(user);
    return userMapper.UserToDeactivateResDto(user);
  }

}
