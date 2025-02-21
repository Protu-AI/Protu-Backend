package com.hazem.notificationservice.dto;

import java.sql.Timestamp;

public record RabbitMQMessage(String messageId, String to, String from, Template template, MetaData metaData) {
  public record Template(String id, EmailData data) {
    public record EmailData(String username, String otp, int otpTtl) {}
  }
  public record MetaData(String service, Timestamp timestamp) {}
}
