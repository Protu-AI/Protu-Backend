package org.protu.userservice.exceptions.custom;

public class OldAndNewPasswordMatchException extends RuntimeException {
  public OldAndNewPasswordMatchException(String message) {
    super(message);
  }
}
