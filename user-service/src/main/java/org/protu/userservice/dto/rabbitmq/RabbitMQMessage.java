package org.protu.userservice.dto.rabbitmq;

import java.sql.Timestamp;

public record RabbitMQMessage<T>(String messageId, String to, String from, Template<T> template, MetaData metaData) {
  public record Template<T>(Integer id, T data) {}
  public record MetaData(String service, Timestamp timestamp) {}
}