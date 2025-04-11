package org.protu.userservice.dto.rabbitmq;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public record RabbitMessage<T>(String messageId, String name, String type, T data, MetaData metaData) {

  public RabbitMessage(String name, String type, T data) {
    this(UUID.randomUUID().toString(), name, type, data,
        new MetaData("user-service", Timestamp.from(Instant.now())));
  }

  public record MetaData(String service, Timestamp timestamp) {
  }
}