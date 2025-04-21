package org.protu.notificationservice.helper;

import org.protu.notificationservice.dto.EmailData;

import java.util.Map;

public interface TemplateProcessor {

  Map<String, Object> getVariables(EmailData message);

  String loadTemplate(Map<String, Object> variables);
}
