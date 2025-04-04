package org.protu.contentservice.common.helpers;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.progress.user.User;
import org.protu.contentservice.progress.user.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHelper {
  private final UserRepository userRepository;

  public User fetchUserByIdOrThrow(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
  }
}
