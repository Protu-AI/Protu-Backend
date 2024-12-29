package org.protu.userservice.constants;

public enum FailureMessages {
  USER_NOT_FOUND("The user with %s: %s does not exist."),
  USER_ALREADY_EXISTS("The user with %s: %s already exists."),
  BAD_CREDENTIALS("Incorrect %s. Please check and try again."),
  EMAIL_NOT_VERIFIED("Your email is registered but not verified. A new verification email has been sent to your inbox."),
  EMAIL_ALREADY_VERIFIED("Your email is already verified."),
  UNAUTHORIZED_ACCESS("You do not have permission to access this resource."),
  INVALID_VERIFICATION_CODE("The verification code entered: %s is invalid. Please check and try again."),
  EXPIRED_VERIFICATION_CODE("The verification code entered: %s has expired. Please request a new verification code."),
  MAIL_SENDING_FAILED("Failed to send email. Please try again later."),
  SESSION_EXPIRED("Access token has expired. Try to refresh. If the refresh token has also expired, the user will need to log in again.");

  private final String message;

  FailureMessages(String message) {
    this.message = message;
  }

  public String getMessage(Object... args) {
    return String.format(message, args);
  }
}
