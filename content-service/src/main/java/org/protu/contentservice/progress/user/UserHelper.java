package org.protu.contentservice.progress.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserHelper {
  private final UserRepository userRepository;

  public User fetchUserByIdOrThrow(Long userId) {
    return userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
  }
}
