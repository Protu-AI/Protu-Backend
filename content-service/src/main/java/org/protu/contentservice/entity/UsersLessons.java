package org.protu.contentservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users_lessons")
@Data
public class UsersLessons {

  @EmbeddedId
  private UsersLessonsId id;

  @Column(name = "is_completed", nullable = false)
  private Boolean isCompleted = false;
}
