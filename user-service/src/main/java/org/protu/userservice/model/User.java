package org.protu.userservice.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_username", columnList = "username"),
        @Index(name = "idx_users_email", columnList = "email"),
        @Index(name = "idx_users_public_id", columnList = "public_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_id_gen")
  @SequenceGenerator(name = "users_id_gen", sequenceName = "users_user_id_seq", allocationSize = 1)
  @Column(name = "id")
  Long id;

  @Column(name = "public_id", nullable = false, unique = true)
  private String publicId;

  @Column(name = "first_name", length = 50, nullable = false)
  String firstName;

  @Column(name = "last_name", length = 50, nullable = false)
  String lastName;

  @Column(name = "username", length = 50, unique = true, nullable = false)
  String username;

  @Column(name = "email", length = 100, unique = true, nullable = false)
  String email;

  @Column(name = "password", length = 100, nullable = false)
  String password;

  @Column(name = "phone_number", length = 20, nullable = false)
  String phoneNumber;

  @Column(name = "authorities", nullable = false, columnDefinition = "TEXT")
  String authorities;

  @Column(name = "is_active", nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
  Boolean isActive = true;

  @Column(name = "is_email_verified", nullable = false)
  Boolean isEmailVerified;

  @Column(name = "verification_code", nullable = false)
  String verificationCode;

  @Column(name = "code_expiry_date", nullable = false)
  Timestamp codeExpiryDate;

  @Column(name = "created_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP", updatable = false)
  Timestamp createdAt;

  @Column(name = "updated_at", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
  Timestamp updatedAt;


  @PrePersist
  protected void onCreate() {
    this.createdAt = Timestamp.valueOf(LocalDateTime.now());
    this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Timestamp.valueOf(LocalDateTime.now());
  }
}