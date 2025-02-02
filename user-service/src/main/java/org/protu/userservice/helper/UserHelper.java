package org.protu.userservice.helper;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.constants.FailureMessages;
import org.protu.userservice.exceptions.custom.UserAlreadyExistsException;
import org.protu.userservice.exceptions.custom.UserNotFoundException;
import org.protu.userservice.model.User;
import org.protu.userservice.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserHelper {
  private final UserRepository userRepo;

  public Optional<User> findUserByIdentifier(String identifier, Optional<String> type) {
    if (type.isEmpty())
      return userRepo.findByUsername(identifier).or(() -> userRepo.findByEmail(identifier));
    return type.get().equals("email") ? userRepo.findByEmail(identifier) : userRepo.findByUsername(identifier);
  }

  public void checkIfUserExists(String identifier, String type) {
    findUserByIdentifier(identifier, Optional.of(type)).ifPresent( user -> {
      throw new UserAlreadyExistsException(FailureMessages.USER_ALREADY_EXISTS.getMessage(type,identifier));
    });
  }

  public User fetchUserOrThrow(String identifier, String type){
    return findUserByIdentifier(identifier,Optional.empty())
        .orElseThrow(() -> new UserNotFoundException(FailureMessages.USER_NOT_FOUND.getMessage(type, identifier)));
  }

  public User fetchUserByIdOrThrow(String userId){
    return userRepo.findByPublicId(userId)
        .orElseThrow(() -> new UserNotFoundException(FailureMessages.USER_NOT_FOUND.getMessage("id", userId)));
  }
}
