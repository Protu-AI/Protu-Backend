package org.protu.contentservice.common.exception.custom;

import org.protu.contentservice.common.enums.FailureMessage;

public class UserAlreadyExistsException extends RuntimeException {
  public UserAlreadyExistsException(Object... args) {
    super(FailureMessage.USER_ALREADY_EXISTS.getMessage(args));
  }
}
