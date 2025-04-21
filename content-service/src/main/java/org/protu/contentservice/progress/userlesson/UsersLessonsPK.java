package org.protu.contentservice.progress.userlesson;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsersLessonsPK implements Serializable {

  private Long userId;
  private Integer lessonId;

  public UsersLessonsPK() {
  }

  public UsersLessonsPK(Long userId, Integer lessonId) {
    this.userId = userId;
    this.lessonId = lessonId;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    UsersLessonsPK that = (UsersLessonsPK) o;
    return Objects.equals(userId, that.userId) && Objects.equals(lessonId, that.lessonId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, lessonId);
  }
}
