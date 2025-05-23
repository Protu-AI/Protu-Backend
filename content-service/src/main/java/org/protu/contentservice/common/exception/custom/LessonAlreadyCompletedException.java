package org.protu.contentservice.common.exception.custom;

import org.protu.contentservice.common.enums.FailureMessage;

public class LessonAlreadyCompletedException extends RuntimeException {
  public LessonAlreadyCompletedException() {
    super(FailureMessage.LESSON_ALREADY_COMPLETED.getMessage());
  }
}
