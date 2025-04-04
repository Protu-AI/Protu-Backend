package org.protu.contentservice.progress.usercourse;

public class UserNotEnrolledInCourse extends RuntimeException {
  public UserNotEnrolledInCourse(String message) {
    super(message);
  }
}
