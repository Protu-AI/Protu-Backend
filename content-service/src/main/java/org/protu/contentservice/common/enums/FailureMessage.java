package org.protu.contentservice.common.enums;

public enum FailureMessage {
  ENTITY_NOT_FOUND("%s: %s is not found"),
  USER_NOT_ENROLLED_IN_COURSE("User is not enrolled in this course"),
  User_NOT_FOUND("User is not found"),
  COURSE_HAS_NO_LESSONS("No lessons found in this course"),
  LESSON_ALREADY_COMPLETED("Lesson is already completed"),
  LESSON_ALREADY_UNCOMPLETED("Lesson is already not completed");


  private final String message;

  FailureMessage(String message) {
    this.message = message;
  }

  public String getMessage(Object... args) {
    return String.format(message, args);
  }
}
