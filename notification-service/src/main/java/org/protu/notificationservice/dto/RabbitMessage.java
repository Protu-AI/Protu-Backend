package org.protu.notificationservice.dto;

import java.sql.Timestamp;

public record RabbitMessage<T>(String messageId, String name, String type, T data, MetaData metaData) {
  public record MetaData(String service, Timestamp timestamp) {
  }
}
