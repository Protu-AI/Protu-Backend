package org.protu.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.notificationservice.dto.RabbitMQMessage;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {
  private final EmailService emailService;

  @RabbitListener(queues = "notification.email.queue")
  public void receiveMessage(RabbitMQMessage rabbitMQMessage) {
    try {
      emailService.prepareAndSendEmail(rabbitMQMessage);
    } catch (Exception e) {
      throw new AmqpRejectAndDontRequeueException("Failed to process this message.");
    }
  }
}
