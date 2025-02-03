package org.protu.userservice.exceptions.custom;

public class InvalidOrExpiredOtpException extends RuntimeException {
  public InvalidOrExpiredOtpException(String s) {
    super(s);
  }
}
