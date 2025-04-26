package org.protu.contentservice.common.exception.custom;

import org.protu.contentservice.progress.enums.FailureMessage;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(Object... args) {
    super(FailureMessage.User_NOT_FOUND.getMessage(args));
  }
}
