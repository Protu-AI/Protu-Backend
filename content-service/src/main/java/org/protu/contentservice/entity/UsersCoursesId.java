package org.protu.contentservice.entity;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsersCoursesId implements Serializable {

  private Integer userId;

  private Integer lessonId;

  public UsersCoursesId(Integer userId, Integer lessonId) {
    this.userId = userId;
    this.lessonId = lessonId;
  }

  public UsersCoursesId() {

  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    UsersCoursesId that = (UsersCoursesId) o;
    return Objects.equals(userId, that.userId) && Objects.equals(lessonId, that.lessonId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, lessonId);
  }
}
