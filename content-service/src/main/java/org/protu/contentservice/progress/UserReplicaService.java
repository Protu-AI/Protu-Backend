package org.protu.contentservice.progress;

import org.protu.contentservice.common.exception.custom.UserNotFoundException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserReplicaService {

  private final JdbcClient jdbcClient;

  public UserReplicaService(JdbcClient jdbcClient) {
    this.jdbcClient = jdbcClient;
  }

  public User getUserById(Long userId) {
    Optional<User> userOpt = jdbcClient.sql("SELECT id, public_id AS publicId, roles FROM users WHERE id = :id")
        .param("id", userId)
        .query(User.class)
        .optional();

    return userOpt.orElseThrow(UserNotFoundException::new);
  }

  public void addUser(UserData userData) {
    jdbcClient.sql("INSERT INTO users (id, public_id, roles) VALUES (:id, :publicId, :roles) ON CONFLICT (id) DO NOTHING")
        .param("id", userData.id())
        .param("publicId", userData.publicId())
        .param("roles", userData.roles())
        .update();
  }

  public void updateUserRoles(UserData userData) {
    jdbcClient.sql("UPDATE users SET roles = :roles WHERE id = :id")
        .param("id", userData.id())
        .param("roles", userData.roles())
        .update();
  }

  public void deleteUser(Long id) {
    jdbcClient.sql("DELETE FROM users WHERE id = :id")
        .param("id", id)
        .update();
  }
}
