package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.helper.UserHelper;
import org.protu.userservice.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
  private final UserHelper userHelper;

  @Override
  public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
    Long userId2 = Long.parseLong(userId);
    User user = userHelper.fetchUserByIdOrThrow(userId2);

    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .authorities(user.getAuthorities().split(","))
        .accountExpired(false)
        .accountLocked(false)
        .credentialsExpired(false)
        .disabled(false)
        .build();
  }
}
