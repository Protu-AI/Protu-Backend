package org.protu.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.notificationservice.config.EmailRabbitMQProperties;
import org.protu.notificationservice.dto.RabbitMQMessage;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailNotificationConsumer {
  private final EmailService emailService;
  private final EmailRabbitMQProperties emailProperties;
  private final RabbitTemplate rabbitTemplate;

  @RabbitListener(queues = "${app.rabbitmq.email.queue.main}")
  public void receiveMessage(RabbitMQMessage message, Message rabbitmqMessage) {
    MessageProperties properties = rabbitmqMessage.getMessageProperties();
    long retryCounts = properties.getRetryCount();
    try {
      emailService.prepareAndSendEmail(message);
    } catch (Exception e) {
      if (retryCounts >= emailProperties.retry().count()) {
        rabbitTemplate.convertAndSend(emailProperties.queue().dead(), message);
        return;
      }

      properties.setRetryCount(retryCounts + 1);
      throw new AmqpRejectAndDontRequeueException("retrial #" + properties.getRetryCount() + " on timestamp: " + Timestamp.from(Instant.now()));
    }
  }
}
