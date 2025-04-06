package org.protu.contentservice.common.exception.custom;

import org.protu.contentservice.common.enums.FailureMessage;

public class EntityAlreadyExistsException extends RuntimeException {
  public EntityAlreadyExistsException(Object... args) {
    super(FailureMessage.ENTITY_ALREADY_EXISTS.getMessage(args));
  }
}
