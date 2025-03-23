package org.protu.contentservice.track.enums;

public enum TrackFailureMessage {
  TRACK_NOT_FOUND("Track with name: %s is not found"),
  TRACK_ALREADY_EXISTS("Track with name: %s already exists");


  private final String message;

  TrackFailureMessage(String message) {
    this.message = message;
  }

  public String getMessage(Object... args) {
    return String.format(message, args);
  }
}
