package org.protu.userservice.dto.rabbitmq;

public record EmailData(String templateId, String to, String from, String username, Otp otp) {

  public record Otp(String value, String ttlInMinutes) {
  }
}
