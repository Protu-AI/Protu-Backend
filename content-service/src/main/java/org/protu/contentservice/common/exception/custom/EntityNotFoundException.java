package org.protu.contentservice.common.exception.custom;

import org.protu.contentservice.common.enums.FailureMessage;

public class EntityNotFoundException extends RuntimeException {
  public EntityNotFoundException(Object... args) {
    super(FailureMessage.ENTITY_NOT_FOUND.getMessage(args));
  }
}
