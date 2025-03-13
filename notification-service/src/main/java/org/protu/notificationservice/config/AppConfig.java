package org.protu.notificationservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailRabbitMQProperties.class)
public class AppConfig {
}
