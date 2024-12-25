package org.protu.userservice.exceptions.custom;

public class UserEmailNotVerifiedException extends RuntimeException {
  public UserEmailNotVerifiedException(String s) {
    super(s);
  }
}
