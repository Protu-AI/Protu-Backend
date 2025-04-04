package org.protu.contentservice.progress.usercourse;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UsersCoursesPK implements Serializable {

  private Long userId;
  private Integer courseId;

  public UsersCoursesPK(Long userId, Integer courseId) {
    this.userId = userId;
    this.courseId = courseId;
  }

  public UsersCoursesPK() {
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    UsersCoursesPK that = (UsersCoursesPK) o;
    return Objects.equals(userId, that.userId) && Objects.equals(courseId, that.courseId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, courseId);
  }
}
