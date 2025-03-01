package org.protu.notificationservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "templates")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Template {
  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Integer id;

  @Column(name = "name", nullable = false, unique = true)
  String name;

  @Column(name = "subject", nullable = false)
  String subject;

  @Column(name = "body", columnDefinition = "TEXT", nullable = false)
  String body;

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