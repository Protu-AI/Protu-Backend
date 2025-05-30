package org.protu.notificationservice.config;

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
public class RabbitConfig {

  private final AppProperties props;

  public RabbitConfig(AppProperties props) {
    this.props = props;
  }

  @Bean
  public Queue emailMainQueue() {
    return QueueBuilder.durable(props.queue().email().main())
        .deadLetterExchange("")
        .deadLetterRoutingKey(props.queue().email().retry())
        .build();
  }

  @Bean
  public Queue emailRetryQueue() {
    return QueueBuilder.durable(props.queue().email().retry())
        .deadLetterExchange("")
        .deadLetterRoutingKey(props.queue().email().main())
        .ttl((int) props.retry().delay())
        .build();
  }

  @Bean
  public Queue emailDeadQueue() {
    return QueueBuilder.durable(props.queue().email().dead()).build();
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