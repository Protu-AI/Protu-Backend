package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserDetailsForAdminDto;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.mapper.UserMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

  private final UserHelper userHelper;
  private final UserMapper userMapper;
  private final UserRepository userRepo;

  public DeactivateResDto deactivateUser(String userId, String token) {
    userHelper.checkIfUserIsAdminOrThrow(token);
    User user = userHelper.fetchUserByIdOrThrow(userId);
    user.setIsActive(false);
    userRepo.save(user);
    return userMapper.toDeactivateDto(user, "ADMIN", "User account is not active for a long period", true);
  }

  public void activateUser(String userId, String token) {
    userHelper.checkIfUserIsAdminOrThrow(token);
    User user = userHelper.fetchUserByIdOrThrow(userId);
    user.setIsActive(true);
    userRepo.save(user);
  }

  public UserDetailsForAdminDto getUserDetailsForAdmin(String userId, String token) {
    userHelper.checkIfUserIsAdminOrThrow(token);
    User user = userHelper.fetchUserByIdOrThrow(userId);
    return userMapper.toUserDetailsForAdmin(user, user.getRoles());
  }

  public UserDetailsForAdminDto addRoleToUser(String role, String userId, String token) {
    userHelper.checkIfUserIsAdminOrThrow(token);
    User user = userHelper.fetchUserByIdOrThrow(userId);
    Set<String> userRoles = Arrays.stream(user.getRoles().split(",")).collect(Collectors.toSet());
    userRoles.add("ROLE_" + role);
    user.setRoles(String.join(",", userRoles));
    userRepo.save(user);
    return userMapper.toUserDetailsForAdmin(user, user.getRoles());
  }

  public void deleteUser(String userId, String token) {
    userHelper.checkIfUserIsAdminOrThrow(token);
    userRepo.findByPublicId(userId).ifPresent(userRepo::delete);
  }
}
