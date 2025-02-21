package org.protu.userservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.dto.rabbitmq.RabbitMQMessage;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQProducer {
  private final RabbitTemplate rabbitTemplate;
  private final Queue queue;

  public void send(RabbitMQMessage queueMessage) {
    rabbitTemplate.convertAndSend(queue.getName(), queueMessage);
  }
}
