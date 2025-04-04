package org.protu.contentservice.progress.user;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_public_id", columnList = "public_id")})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
  @Id
  @Column(name = "id")
  Long id;

  @Column(name = "public_id", nullable = false, unique = true)
  String publicId;

  @Column(name = "roles", nullable = false, columnDefinition = "TEXT")
  String roles;
}