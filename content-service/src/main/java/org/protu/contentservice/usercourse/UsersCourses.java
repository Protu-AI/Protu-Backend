package org.protu.contentservice.usercourse;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "users_courses")
@Data
public class UsersCourses {

  @EmbeddedId
  private UsersCoursesId id;

  @Column(name = "completed_lessons", nullable = false)
  private Integer completedLessons = 0;
}
