package org.protu.notificationservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@RequiredArgsConstructor
public class RabbitMQConfig {
  
  private final EmailRabbitMQProperties rabbitMQProperties;
  private final EmailRabbitMQProperties properties;

  @Bean
  public Queue emailMainQueue() {
    return QueueBuilder.durable(properties.queue().main())
        .deadLetterExchange("")
        .deadLetterRoutingKey(properties.queue().retry())
        .build();
  }

  @Bean
  public Queue emailRetryQueue() {
    return QueueBuilder.durable(properties.queue().retry())
        .deadLetterExchange("")
        .deadLetterRoutingKey(properties.queue().main())
        .ttl((int) rabbitMQProperties.retry().delay())
        .build();
  }

  @Bean
  public Queue emailDeadQueue() {
    return QueueBuilder.durable(properties.queue().dead()).build();
  }

  @Bean
  public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  @Bean
  public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
      ConnectionFactory connectionFactory) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(jackson2JsonMessageConverter());
    factory.setDefaultRequeueRejected(false);
    return factory;
  }
}