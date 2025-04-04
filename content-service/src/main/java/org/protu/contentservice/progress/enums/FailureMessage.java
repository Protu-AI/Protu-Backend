package org.protu.contentservice.progress.enums;

public enum FailureMessage {
  USER_NOT_ENROLLED_IN_COURSE("User is not enrolled in this course");


  private final String message;

  FailureMessage(String message) {
    this.message = message;
  }

  public String getMessage(Object... args) {
    return String.format(message, args);
  }
}
