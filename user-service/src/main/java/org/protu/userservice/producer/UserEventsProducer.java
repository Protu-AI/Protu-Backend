package org.protu.userservice.producer;

import lombok.RequiredArgsConstructor;
import org.protu.userservice.config.AppProperties;
import org.protu.userservice.dto.rabbitmq.RabbitMessage;
import org.protu.userservice.dto.rabbitmq.UserData;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserEventsProducer {

  private final RabbitTemplate rabbitTemplate;
  private final AppProperties props;

  public void send(RabbitMessage<UserData> rabbitMessage, String routingKey) {
    final String USER_EVENTS_EXCHANGE = props.rabbit().exchange().userEvents();
    rabbitTemplate.convertAndSend(USER_EVENTS_EXCHANGE, routingKey, rabbitMessage
        , message -> {
          message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
          return message;
        });
  }
}
