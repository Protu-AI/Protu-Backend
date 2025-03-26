package org.protu.contentservice.common.enums;

public enum FailureMessage {
  ENTITY_NOT_FOUND("%s with name: %s is not found"),
  ENTITY_ALREADY_EXISTS("%s with name: %s already exists");


  private final String message;

  FailureMessage(String message) {
    this.message = message;
  }

  public String getMessage(Object... args) {
    return String.format(message, args);
  }
}
