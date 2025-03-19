package org.protu.userservice.helper;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.dto.response.TokensResDto;
import org.protu.userservice.exceptions.custom.UnauthorizedAccessException;
import org.protu.userservice.exceptions.custom.UserAlreadyExistsException;
import org.protu.userservice.exceptions.custom.UserNotFoundException;
import org.protu.userservice.mapper.TokenMapper;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.protu.userservice.service.JWTService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserHelper {
  private final UserRepository userRepo;
  private final TokenMapper tokenMapper;
  private final AppProperties properties;
  private final JWTService jwtService;
  private final RedisTemplate<Object, String> redisTemplate;

  public Optional<User> findUserByIdentifier(String identifier, Optional<String> type) {
    if (type.isEmpty())
      return userRepo.findByUsername(identifier).or(() -> userRepo.findByEmail(identifier));
    return type.get().equals("email") ? userRepo.findByEmail(identifier) : userRepo.findByUsername(identifier);
  }

  public void checkIfUserExists(String identifier, String type) {
    findUserByIdentifier(identifier, Optional.of(type)).ifPresent(user -> {
      throw new UserAlreadyExistsException(FailureMessages.USER_ALREADY_EXISTS.getMessage(type, identifier));
    });
  }

  public User fetchUserOrThrow(String identifier, String type) {
    return findUserByIdentifier(identifier, Optional.empty())
        .orElseThrow(() -> new UserNotFoundException(FailureMessages.USER_NOT_FOUND.getMessage(type, identifier)));
  }

  public User fetchUserByIdOrThrow(String userId) {
    return userRepo.findByPublicId(userId)
        .orElseThrow(() -> new UserNotFoundException(FailureMessages.USER_NOT_FOUND.getMessage("id", userId)));
  }

  public TokensResDto markUserEmailVerified(User user) {
    redisTemplate.opsForValue().getAndDelete(properties.otp().prefix().email() + user.getId());
    user.setIsEmailVerified(true);
    userRepo.save(user);
    return tokenMapper.toTokensDto(user, jwtService);
  }

  public void verifyUserAuthority(String userId, String authUserId) {
    if (!userId.equals(authUserId)) {
      throw new UnauthorizedAccessException(FailureMessages.UNAUTHORIZED_ACCESS.getMessage());
    }
  }

  public void checkIfUserIsAdminOrThrow(String token) {
    String userRoles = jwtService.getUserRoles(token);
    boolean hasAdminRole = Arrays.asList(userRoles.split(",")).contains("ROLE_ADMIN");
    if (!hasAdminRole) {
      throw new UnauthorizedAccessException("You don't have permission to do such operation");
    }
  }
}
