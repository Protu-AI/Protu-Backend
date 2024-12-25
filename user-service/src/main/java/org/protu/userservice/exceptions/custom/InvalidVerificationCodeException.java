package org.protu.userservice.exceptions.custom;

public class InvalidVerificationCodeException extends RuntimeException {
  public InvalidVerificationCodeException(String s) {
    super(s);
  }
}
