package org.protu.userservice.service;

import org.protu.userservice.config.AppProperties;
import org.protu.userservice.dto.rabbitmq.RabbitMessage;
import org.protu.userservice.dto.rabbitmq.UserData;
import org.protu.userservice.dto.response.DeactivateResDto;
import org.protu.userservice.dto.response.UserDetailsForAdminDto;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.mapper.UserMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.producer.UserEventsProducer;
import org.protu.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminService {

  private final UserHelper userHelper;
  private final UserMapper userMapper;
  private final UserRepository userRepo;
  private final UserEventsProducer userEventsProducer;
  private final String USER_UPDATED;
  private final String USER_DELETED;

  public AdminService(UserHelper userHelper, UserMapper userMapper, UserRepository userRepo, UserEventsProducer userEventsProducer, AppProperties props) {
    this.userHelper = userHelper;
    this.userMapper = userMapper;
    this.userRepo = userRepo;
    this.userEventsProducer = userEventsProducer;
    USER_UPDATED = props.rabbit().routingKey().userUpdated();
    USER_DELETED = props.rabbit().routingKey().userDeleted();
  }

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
    userEventsProducer.send(
        new RabbitMessage<>(USER_UPDATED, "event",
            new UserData(user.getId(), user.getPublicId(), user.getRoles())), USER_UPDATED);
    return userMapper.toUserDetailsForAdmin(user, user.getRoles());
  }

  public void deleteUser(String userId, String token) {
    userHelper.checkIfUserIsAdminOrThrow(token);
    userRepo.findByPublicId(userId).ifPresent(user -> {
      userEventsProducer.send(
          new RabbitMessage<>(USER_DELETED, "event",
              new UserData(user.getId(), user.getPublicId(), user.getRoles())), USER_DELETED);
      userRepo.delete(user);
    });
  }
}
