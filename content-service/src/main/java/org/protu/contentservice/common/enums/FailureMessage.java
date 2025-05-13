package org.protu.contentservice.common.enums;

public enum FailureMessage {
  ENTITY_NOT_FOUND("%s: %s is not found"),
  ENTITY_ALREADY_EXISTS("%s: %s already exists"),

  USER_NOT_ENROLLED_IN_COURSE("User is not enrolled in this course"),
  USER_ALREADY_EXISTS("User already exists"),
  User_NOT_FOUND("User is not found");

  private final String message;

  FailureMessage(String message) {
    this.message = message;
  }

  public String getMessage(Object... args) {
    return String.format(message, args);
  }
}
