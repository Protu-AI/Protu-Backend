package org.protu.contentservice.common.exception.custom;

import org.protu.contentservice.common.enums.FailureMessage;

public class CourseHasNoLessonsException extends RuntimeException {
  public CourseHasNoLessonsException(Object... args) {
    super(FailureMessage.COURSE_HAS_NO_LESSONS.getMessage(args));
  }
}
