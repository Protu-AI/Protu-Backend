package org.protu.notificationservice.dto;

public record EmailData(String templateId, String to, String from, String username, Otp otp) {
  public record Otp(String value, String ttlInMinutes) {
  }
}
