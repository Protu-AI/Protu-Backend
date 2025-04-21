package org.protu.contentservice.progress.user;

import lombok.RequiredArgsConstructor;
import org.protu.contentservice.common.exception.custom.UserAlreadyExistsException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserReplicaService {

  private final UserRepository userRepo;

  public void addUser(UserData userData) {
    userRepo.findById(userData.id()).ifPresent(user -> {
      throw new UserAlreadyExistsException();
    });

    userRepo.findByPublicId(userData.publicId()).ifPresent(user -> {
      throw new UserAlreadyExistsException();
    });

    User user = User.builder()
        .id(userData.id())
        .publicId(userData.publicId())
        .roles(userData.roles())
        .build();

    userRepo.save(user);
  }

  public void updateUserRoles(UserData userData) {
    User user = userRepo.findById(userData.id()).orElse(
        userRepo.save(new User(userData.id(), userData.publicId(), userData.roles()))
    );

    user.setRoles(userData.roles());
    userRepo.save(user);
  }

  public void deleteUser(Long userId) {
    userRepo.findById(userId).ifPresent(userRepo::delete);
  }
}
