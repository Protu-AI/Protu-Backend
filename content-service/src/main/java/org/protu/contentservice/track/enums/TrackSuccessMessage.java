package org.protu.contentservice.track.enums;

public enum TrackSuccessMessage {
  GET_ALL_TRACKS("Tracks details are retrieved successfully"),
  GET_SINGLE_TRACK("Track details are retrieved successfully"),
  CREATE_NEW_TRACK("A new track has been created successfully"),
  UPDATE_TRACK("Tracks details have been updated successfully");

  public final String message;

  TrackSuccessMessage(String message) {
    this.message = message;
  }
}
