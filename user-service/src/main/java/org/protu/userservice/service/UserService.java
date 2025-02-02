package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.request.FullUpdateReqDto;
import org.protu.userservice.dto.request.PartialUpdateReqDto;
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

  public void verifyUserAuthority(String userId, String authUserId) {
    if (!userId.equals(authUserId)) {
      throw new UnauthorizedAccessException(FailureMessages.UNAUTHORIZED_ACCESS.getMessage());
    }
  }

  public UserResDto getUserById(String userId, String authUserId) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    verifyUserAuthority(userId, authUserId);
    return userMapper.toUserDto(user);
  }

  public UserResDto fullUpdateUser(String userId, String authUserId, FullUpdateReqDto fullUpdateReqDto) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    verifyUserAuthority(userId, authUserId);
    userHelper.checkIfUserExists(fullUpdateReqDto.username(),"username");
    user.setUsername(fullUpdateReqDto.username());
    user.setFirstName(fullUpdateReqDto.firstName());
    user.setLastName(fullUpdateReqDto.lastName());
    user.setPhoneNumber(fullUpdateReqDto.phoneNumber());
    user.setPassword(passwordEncoder.encode(fullUpdateReqDto.password()));

    userRepo.save(user);
    return userMapper.toUserDto(user);
  }

  public UserResDto partialUpdateUser(String userId, String authUserId, PartialUpdateReqDto partialUpdateReqDto) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    verifyUserAuthority(userId, authUserId);
    if (partialUpdateReqDto.username() != null) {
      userHelper.checkIfUserExists(partialUpdateReqDto.username(),"username");
      user.setUsername(partialUpdateReqDto.username());
    }
    if (partialUpdateReqDto.firstName() != null) {
      user.setFirstName(partialUpdateReqDto.firstName());
    }
    if (partialUpdateReqDto.lastName() != null) {
      user.setLastName(partialUpdateReqDto.lastName());
    }
    if (partialUpdateReqDto.phoneNumber() != null) {
      user.setPhoneNumber(partialUpdateReqDto.phoneNumber());
    }
    userRepo.save(user);
    return userMapper.toUserDto(user);
  }

  public DeactivateResDto deactivateUser(String userId, String authUserId) {
    User user = userHelper.fetchUserByIdOrThrow(userId);
    verifyUserAuthority(userId, authUserId);
    user.setIsActive(false);
    userRepo.save(user);
    return userMapper.toDeactivateDto(user);
  }

}
