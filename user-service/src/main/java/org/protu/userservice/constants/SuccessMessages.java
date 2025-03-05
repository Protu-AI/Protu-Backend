package org.protu.userservice.constants;

public enum SuccessMessages {
  SIGN_UP_MSG("User registration is successful. A new verification code is sent to your inbox. Please check your email to proceed."),
  VERIFY_MSG("Email verified successfully. Your account is now active."),
  VALIDATE_MSG("The username or email you provided is valid. Please proceed to the next step."),
  SIGN_IN_MSG("Welcome back! You have successfully logged in."),
  REFRESH_MSG("A new access token is generated successfully."),
  FORGOT_PASSWORD_MSG("If the provided email exists, a new password reset verification code is sent to your inbox. Please check your email to proceed."),
  RESET_PASSWORD_MSG("Password is reset successfully."),
  GET_USER_MSG("User details are retrieved successfully."),
  UPDATE_USER_MSG("User profile is updated successfully."),
  DEACTIVATE_USER_MSG("User account is deactivated successfully."),
  NEW_CODE_MSG("If the provided email exists, a new verification code will be sent to your inbox. Please check your email to proceed."),
  UPLOAD_PROFILE_PIC("Your profile picture is successfully uploaded and updated."),
  CHANGE_PASSWORD_MSG("Password changed successfully."),
  PASSWORD_RESET_OTP_MSG("Password reset OTP is verified successfully");

  public final String message;

  SuccessMessages(String message) {
    this.message = message;
  }
}
