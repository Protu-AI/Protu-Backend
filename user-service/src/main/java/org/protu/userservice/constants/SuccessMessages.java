package org.protu.userservice.constants;

public enum SuccessMessages {
  SIGN_UP_MSG("User registration successful. If the verification email doesn't appear in your inbox, please check your spam folder or try again later."),
  VERIFY_MSG("Email verified successfully. Your account is now active."),
  VALIDATE_MSG("The username or email you provided is valid. Please proceed to the next step."),
  SIGN_IN_MSG("Welcome back! You have successfully logged in."),
  REFRESH_MSG("Access token has been refreshed successfully."),
  FORGOT_PASSWORD_MSG("If the provided email exists, a password reset verification code will be sent to your inbox. Please check your email to proceed."),
  RESET_PASSWORD_MSG("Password reset successfully."),
  GET_USER_MSG("User details retrieved successfully."),
  UPDATE_USER_MSG("User profile has been updated successfully."),
  DEACTIVATE_USER_MSG("User account has been deactivated successfully."),
  NEW_CODE_MSG("If the provided email exists, a new verification code will be sent to your inbox. Please check your email to proceed.");

  public final String message;

  SuccessMessages(String message) {
    this.message = message;
  }
}
