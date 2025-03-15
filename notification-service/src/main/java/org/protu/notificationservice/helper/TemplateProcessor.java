package org.protu.notificationservice.helper;

import org.protu.notificationservice.dto.RabbitMQMessage;

import java.util.Map;

public interface TemplateProcessor {
  
  Map<String, Object> getVariables(RabbitMQMessage message);

  String loadTemplate(Map<String, Object> variables);
}
