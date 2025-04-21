package org.protu.contentservice.progress.usercourse;

import org.protu.contentservice.progress.enums.FailureMessage;

public class UserNotEnrolledInCourseException extends RuntimeException {
  public UserNotEnrolledInCourseException() {
    super(FailureMessage.USER_NOT_ENROLLED_IN_COURSE.getMessage());
  }
}
