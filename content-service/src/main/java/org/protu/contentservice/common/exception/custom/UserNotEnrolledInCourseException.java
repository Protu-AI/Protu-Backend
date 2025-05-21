package org.protu.contentservice.common.exception.custom;

import org.protu.contentservice.common.enums.FailureMessage;

public class UserNotEnrolledInCourseException extends RuntimeException {
  public UserNotEnrolledInCourseException() {
    super(FailureMessage.USER_NOT_ENROLLED_IN_COURSE.getMessage());
  }
}
