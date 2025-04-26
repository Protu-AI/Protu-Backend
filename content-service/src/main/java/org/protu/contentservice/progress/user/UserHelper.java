package org.protu.contentservice.progress.user;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.exception.custom.UserNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHelper {
  private final UserRepository userRepository;

  public User fetchUserByIdOrThrow(Long userId) {
    return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
  }

  public void checkIfUserExistsOrThrow(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new UserNotFoundException(userId);
    }
  }
}
