package org.protu.userservice.exceptions.custom;

public class EmailNotVerifiedException extends RuntimeException {
  public EmailNotVerifiedException(String s) {
    super(s);
  }
}
