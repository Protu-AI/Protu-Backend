package org.protu.userservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

  private final AppProperties props;

  @Bean
  public Queue emailMainQueue() {
    return QueueBuilder.durable(props.rabbit().queue().emailMainQueue())
        .deadLetterExchange("")
        .deadLetterRoutingKey(props.rabbit().queue().emailRetryQueue())
        .build();
  }

  @Bean
  public TopicExchange userEventsTopic() {
    return new TopicExchange(props.rabbit().exchange().userEvents(), true, false);
  }

  @Bean
  public Queue contentServiceUserQueue() {
    return QueueBuilder
        .durable(props.rabbit().queue().userReplica())
        .build();
  }

  @Bean
  public Binding userEventsBinding(Queue contentServiceUserQueue, TopicExchange userEventsTopic) {
    return BindingBuilder.bind(contentServiceUserQueue)
        .to(userEventsTopic)
        .with(props.rabbit().routingKey().userPattern());
  }

  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    rabbitTemplate.setMessageConverter(new Jackson2JsonMessageConverter());
    return rabbitTemplate;
  }
}
