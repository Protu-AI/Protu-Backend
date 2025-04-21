package org.protu.userservice.producer;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.dto.rabbitmq.EmailData;
import org.protu.userservice.dto.rabbitmq.RabbitMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailNotificationProducer {
  private final RabbitTemplate rabbitTemplate;
  private final AppProperties props;

  public void send(RabbitMessage<EmailData> message) {
    rabbitTemplate.convertAndSend(props.rabbit().queue().emailMainQueue(), message);
  }
}
