package org.protu.contentservice.common.enums;

public enum SuccessMessage {
  GET_ALL_ENTITIES("%s details are retrieved successfully"),
  GET_SINGLE_ENTITY("%s details are retrieved successfully"),
  CREATE_NEW_ENTITY("A new %s has been created successfully"),
  UPDATE_ENTITY("%s details have been updated successfully"),
  ADD_ENTITY_TO_PARENT_ENTITY("%s %s has been successfully added to the %s %s");

  public final String message;

  SuccessMessage(String message) {
    this.message = message;
  }

  public String getMessage(Object... args) {
    return String.format(message, args);
  }
}
