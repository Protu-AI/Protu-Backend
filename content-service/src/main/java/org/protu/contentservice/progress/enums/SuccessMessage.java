package org.protu.contentservice.progress.enums;

public enum SuccessMessage {
  GET_USER_PROGRESS_IN_COURSE("User progress has been retrieved successfully"),
  USER_ENROLLED_IN_COURSE("User has enrolled in the course successfully"),
  USER_CANCELLED_ENROLLMENT_IN_COURSE("User has cancelled enrollment in the course successfully"),
  USER_COMPLETED_A_COURSE_LESSON("Lesson is marked completed successfully");

  public final String message;

  SuccessMessage(String message) {
    this.message = message;
  }

  public String getMessage(Object... args) {
    return String.format(message, args);
  }
}
