package org.protu.userservice.exceptions.custom;

public class UserEmailAlreadyVerifiedException extends RuntimeException {
  public UserEmailAlreadyVerifiedException(String s) {
    super(s);
  }
}
