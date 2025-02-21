package com.hazem.notificationservice.service;

import com.hazem.notificationservice.dto.RabbitMQMessage;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQConsumer {
  private final EmailService emailService;

  @RabbitListener(queues = "notification.email.queue")
  public void receiveMessage(RabbitMQMessage rabbitMQMessage) throws MessagingException {
    emailService.prepareAndSendEmail(rabbitMQMessage);
  }
}
