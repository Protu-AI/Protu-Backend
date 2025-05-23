package org.protu.contentservice.common.exception.custom;

import org.protu.contentservice.common.enums.FailureMessage;

public class LessonAlreadyNotCompletedException extends RuntimeException {
  public LessonAlreadyNotCompletedException() {
    super(FailureMessage.LESSON_ALREADY_UNCOMPLETED.getMessage());
  }
}
