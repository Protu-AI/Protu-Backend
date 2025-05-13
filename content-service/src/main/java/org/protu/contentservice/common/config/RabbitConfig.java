package org.protu.contentservice.common.config;

import org.protu.contentservice.common.properties.AppProperties;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

  private final AppProperties props;

  public RabbitConfig(AppProperties props) {
    this.props = props;
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

  @Bean
  public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }
}
