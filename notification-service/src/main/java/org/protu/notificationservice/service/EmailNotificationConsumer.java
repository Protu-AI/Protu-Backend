package org.protu.notificationservice.service;

import lombok.RequiredArgsConstructor;
import org.protu.notificationservice.config.AppProperties;
import org.protu.notificationservice.dto.EmailData;
import org.protu.notificationservice.dto.RabbitMessage;
import org.protu.notificationservice.helper.EmailVerificationTemplateProcessor;
import org.protu.notificationservice.helper.PasswordResetTemplateProcessor;
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
  private final AppProperties props;
  private final RabbitTemplate rabbitTemplate;
  private final EmailVerificationTemplateProcessor emailVerificationTemplateProcessor;
  private final PasswordResetTemplateProcessor passwordResetTemplateProcessor;

  @RabbitListener(queues = "${app.rabbit.queue.email.main}")
  public void receiveMessage(RabbitMessage<EmailData> message, Message rabbitmqMessage) {
    MessageProperties properties = rabbitmqMessage.getMessageProperties();
    long retryCounts = properties.getRetryCount();
    try {
      if (message.data().templateId().equals("email-verification")) {
        emailService.prepareAndSendEmail(message.data(), emailVerificationTemplateProcessor);
        return;
      }
      emailService.prepareAndSendEmail(message.data(), passwordResetTemplateProcessor);
    } catch (Exception e) {
      if (retryCounts >= props.retry().count()) {
        rabbitTemplate.convertAndSend(props.queue().email().dead(), message);
        return;
      }

      properties.setRetryCount(retryCounts + 1);
      throw new AmqpRejectAndDontRequeueException("retrial #" + properties.getRetryCount() + " on timestamp: " + Timestamp.from(Instant.now()));
    }
  }
}
