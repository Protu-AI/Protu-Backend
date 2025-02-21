package org.protu.userservice.constants;

public enum FailureMessages {
  USER_NOT_FOUND("The user with %s: %s does not exist."),
  USER_ALREADY_EXISTS("The user with %s: %s already exists."),
  BAD_CREDENTIALS("Incorrect %s. Please check and try again."),
  EMAIL_NOT_VERIFIED("Your email is registered but not verified. A new verification email is sent to your inbox."),
  EMAIL_ALREADY_VERIFIED("Your email is already verified."),
  UNAUTHORIZED_ACCESS("You do not have permission to access this resource."),
  INVALID_OR_EXPIRED_OTP("The entered OTP code: %s is invalid or has expired. Please check and try again."),
  UPLOAD_PROFILE_PIC_FAILURE("There was an error uploading your profile picture. Please try again later");

  private final String message;

  FailureMessages(String message) {
    this.message = message;
  }

  public String getMessage(Object... args) {
    return String.format(message, args);
  }
}
